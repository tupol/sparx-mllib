package com.lunatic.mlx.kddcup99.mllib.modgen

import com.lunatic.mlx.kddcup99.mllib.Configuration
import Configuration._
import com.lunatic.mlx.kddcup99._
import com.lunatic.mlx.kddcup99.mllib.metadata._
import com.lunatic.mlx.kddcup99.mllib.transformers.KddTransformer
import org.apache.spark.SparkContext
import org.apache.spark.mllib.clustering.{KMeans, KMeansModel}
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.rdd.RDD

/**
 * Train KMeans model(s)
 */
class TrainKMeans extends SparkRunnable {

  def run(implicit sc: SparkContext, appConf: Configuration) = {

    val transformer = loadObjectFromFile[KddTransformer](appConf.transformerModelPath("KddTransformer")).get
    val trainNormData = sc.objectFile[(String, Vector)](preparedPath(appConf.trainDataSplitPath)).cache
    val testNormData = sc.objectFile[(String, Vector)](preparedPath(appConf.testDataSplitPath)).cache

    val k_indices = appConf.kmeansTrainingTemplate.cluster_range
    val iteration_indices = appConf.kmeansTrainingTemplate.iteration_range
    val epsilon_indices = appConf.kmeansTrainingTemplate.epsilon_range
    val runs = appConf.kmeansTrainingTemplate.runs

    for (kValue <- k_indices;
         iterations <- iteration_indices;
         epsilon <- epsilon_indices
    ) yield {
      val params = KMeansParams(kValue, epsilon, iterations, runs)
      val (model, trainRuntime_ms) = train(params, trainNormData.map(_._2))
      val modelFile = stubFilePath(params) + ".model"

      println(s"Saving model to $modelFile")
      removeHdfsFile(modelFile)
      // Save just the model... maybe we can decide that the XMD is better than the silly model
      model.save(sc, modelFile)

      //Run evaluation against the original training data split and save the results
      val modelQI = evaluateModel(model, trainNormData, trainRuntime_ms)

      // Save the XMD
      val kMeansXMD = KMeansXMD(params, transformer, model, modelQI)
      saveObjectToFile(kMeansXMD, modelFile + ".xmd")

      val trainingEvaluationReport = evaluationReport(params, modelQI)
      saveLinesToFile(trainingEvaluationReport, stubFilePath(params) + "_train.md")
      println(trainingEvaluationReport.mkString("\n"))

      //Run evaluation against the original testing data split and save the results
      val testQI = evaluateModel(model, testNormData, trainRuntime_ms)
      val testingEvaluationReport = evaluationReport(params, testQI)
      saveLinesToFile(testingEvaluationReport, stubFilePath(params) + "_test.md")
      println(testingEvaluationReport.mkString("\n"))

    }
  }


  def train(params: KMeansParams, vectors: RDD[Vector])
           (implicit sc: SparkContext, appConf: Configuration): (KMeansModel, Long) = {

    val seed = 7774777L

    val trainer = new KMeans().
      setK(params.k).
      setMaxIterations(params.maxIterations).
      setRuns(params.runs).
      setInitializationMode(KMeans.K_MEANS_PARALLEL).
      setEpsilon(params.epsilon).
      setSeed(seed)

    timeCode(trainer.run(vectors))

  }


  private def predict(model: KMeansModel, labeledNormalizedData: RDD[(String, Vector)]): RDD[Prediction] = {

    labeledNormalizedData.map { case (label, vector) =>
      val k = model.predict(vector)
      val distance = Vectors.sqdist(vector, model.clusterCenters(k))

      Prediction(k, distance, "", vector.toArray, label)
    }
  }

  /**
    * Evaluate the model and build a KMeansModelQI quality information instance
    *
    * @param model
    * @param normalizedLabeledData
    * @param trainingRuntime_ms
    * @return
    */
  def evaluateModel(model: KMeansModel, normalizedLabeledData: RDD[(String, Vector)], trainingRuntime_ms: Long):
  KMeansModelQI = {

    // Evaluate clustering by computing Within Set Sum of Squared Errors
    val sse = model.computeCost(normalizedLabeledData.map(_._2))

    val trainingPredictions = predict(model, normalizedLabeledData)

    val lcd = computeDistributionMatrix(model.k, trainingPredictions)
    val distanceStatsByK = computeDistanceStats(model, trainingPredictions)

    val trainMDS = DistanceStats(
      distanceStatsByK.map(_.min).min,
      distanceStatsByK.map(_.avg).sum / model.k,
      distanceStatsByK.map(_.max).max,
      sse
    )

    KMeansModelQI(trainMDS, distanceStatsByK, lcd, trainingRuntime_ms)
  }


  private def computeDistributionMatrix(k: Int, predictions: RDD[Prediction]) = {

    val labelsAndClusters = predictions.map(p => (p.label, p.cluster)).cache

    val clusterCountByLabel = labelsAndClusters.groupByKey.map { case (label, clusters) =>
      val countByCluster = clusters
        .groupBy(x => x).map(x => (x._1, x._2.size))
      val countByClusterSeq = (0 until k).map(ck => countByCluster.getOrElse(ck, 0)).map(_.toLong).toArray

      (label, countByClusterSeq)
    }.collect

    val labels = clusterCountByLabel.map(_._1)
    val labelsXclusters = clusterCountByLabel.map(_._2)

    LabelsClustersDistribution(labels, k, labelsXclusters)

  }

  private def computeDistanceStats(model: KMeansModel, predictions: RDD[Prediction]): Seq[DistanceStats] = {

    predictions.map { pred => (pred.cluster, pred.distance) }.
      groupByKey.
      map { case (clust, dist) =>
        val avgDist = dist.sum / dist.size
        val sse = dist.map(d => d - avgDist).map(d => d * d).sum
        (clust, DistanceStats(dist.min, avgDist, dist.max, sse))
      }.collect.toSeq.sortBy(_._1).map(_._2)
  }

  /**
    * Producxe a nice printable text report in a sort of human readable format
    *
    * @param params
    * @param modelQI
    * @param sc
    * @param appConf
    * @return
    */
  def evaluationReport(params: KMeansParams, modelQI: KMeansModelQI)
                      (implicit sc: SparkContext, appConf: Configuration) = {

    val lcd = modelQI.labelsClustersDistribution

    val dataSize = lcd.total

    val title = f"## Experiment ${params.experimentId}" :: "" :: Nil

    val inputParamsStr =
        f"| Input Parameters     |   Value   |" ::
        f"| :------------------- | --------: |" ::
        f"| Training Data Size   | $dataSize%9d |" ::
        f"| Clusters             | ${params.k}%9d |" ::
        f"| Iterations           | ${params.maxIterations}%9d |" ::
        f"| Runs                 | ${params.runs}%9d |" ::
        f"| Epsilon              | ${params.epsilon}%.3E |" ::
        f"| Normalization        | ${params.norm}%9s |" ::
        Nil

    val tabCountHeaderStr = "" :: "" ::
      "### Count per label per cluster (top 10 Ks)" :: "" ::
      "| Label                |   Total   |  K  | Count   |  K  | Count   |  K  | Count   |  K  | Count   |  K  | Count   |  K  | Count   |  K  | Count   |  K  | Count   |  K  | Count   |  K  | Count   |" ::
      "| -------------------- | --------: | --: | ------: | --: | ------: | --: | ------: | --: | ------: | --: | ------: | --: | ------: | --: | ------: | --: | ------: | --: | ------: | --: | ------: |" ::
      Nil

    val sortedCx = lcd.labels.zip(lcd.totalsByLabel).zip(lcd.labelsXclusters).
      sortBy(_._1._2).reverse

    val sortedLabels = sortedCx.map(_._1._1)
    val sortedLabelTotals = sortedCx.map(_._1._2)
    val sortedLabelCounts = sortedCx.map(_._2)

    val tabCountStr =
      sortedCx.
        map { case ((label, total), counts) =>
          val cwp = counts.zipWithIndex.map(_.swap).sortBy(_._2).reverse.take(10).
            map { case (k, count) =>
              if(count != 0) f"${k}%3d | ${count}%7d"
              else f"    | ${" "}%7s"
            }.mkString(" | ")
          f"| ${label}%-20s | ${total}%9d | ${cwp} |"
        }

    val tabPercentageHeaderStr = "" :: "" ::
      "### Percentage per label per cluster (top 10 Ks)" :: "" ::
      "| Label                |   Total   |  K  | %(tot)  |  K  | %(tot)  |  K  | %(tot)  |  K  | %(tot)  |  K  | %(tot)  |  K  | %(tot)  |  K  | %(tot)  |  K  | %(tot)  |  K  | %(tot)  |  K  | %(tot)  |" ::
      "| -------------------- | --------: | --: | ------: | --: | ------: | --: | ------: | --: | ------: | --: | ------: | --: | ------: | --: | ------: | --: | ------: | --: | ------: | --: | ------: |" ::
      Nil

    val tabPercentagesStr = sortedCx.
        map { case ((label, total), counts) =>
          val cwp = counts.zipWithIndex.map(_.swap).sortBy(_._2).reverse.take(10).
            map { case (k, count) =>
              val ratio = 1.0 * count / total * 100
              if(ratio != 0) f"${k}%3d | ${ratio}%7.3f"
              else f"    | ${" "}%7s"
            }.mkString(" | ")
          f"| ${label}%-20s | ${total}%9d | ${cwp} |"
        }


    val tabCountByClustHeader = "" ::
      "| K     |   Total   | " + sortedLabels.map { label =>
        if (label.length > 7)
          label.substring(0, 6) + "."
        else label
      }.map(s => f"${s}%-7s").mkString(" | ") + " |" ::
      Nil

    val tabCountByClustHeaderSeparator =
      "| ----: | --------: | " + sortedLabels.map(x => "------:").mkString(" | ") + " |" ::
        Nil

    val tabCountByClustFooter =
      f"| Total | ${dataSize}%9d | " + sortedLabelTotals.map(s => f"${s}%7d").mkString(" | ") + " |" ::
        Nil

    val tabCountByClustTitle = "" :: "" ::
      "### Count per cluster per label" :: Nil


    // these are actually just sorted by label, preserving k ordering (0, 1...K)
    val sortedClusterCounts = LabelsClustersDistribution.transpose(sortedLabelCounts)

    val clusters = 0 to params.k

    val tabCountByClust = clusters.zip(lcd.totalsByCluster).zip(sortedClusterCounts).
      map { case ((clust, total), counts) =>
      val countByLabel = counts.
        map { count =>
          if (count != 0) f"$count%7d"
          else f"${""}%7s"
        }.mkString(" | ")
      f"| $clust%5d | $total%9d | $countByLabel |"
    }

    val tabPercentageByClustTitle = "" :: "" ::
      "### Percentage per cluster per label" :: Nil

    val tabPercentageByClust = clusters.zip(lcd.totalsByCluster).zip(sortedClusterCounts).
      map { case ((clust, total), counts) =>
        val percentageByLabel = counts.
          map { count =>
            if (count != 0) f"${1.0 * count / total * 100}%7.3f"
            else f"${""}%7s"
          }.mkString(" | ")
        f"| $clust%5d | $total%9d | $percentageByLabel |"
      }


    val tabClustInfoHeader = "" :: "" ::
      "### Clusters Info" ::
      "" ::
      f"| K     | ${"Labels"}%-10s | ${"Entropy"}%-10s | ${"Purity"}%-10s | ${"Min. Dist."}%-12s | ${"Avg. Dist."}%-12s | ${"Max. Dist."}%-12s | ${"SSE"}%-12s | " ::
      f"| ----: | ---------: | ---------: | ---------: | -----------: | -----------: | -----------: | -----------: | " ::
      Nil


    val tabClustInfo = (0 until params.k).zip(lcd.totalsByCluster).
      zip(lcd.entropyByCluster.zip(lcd.purityByCluster)).
      zip(modelQI.distanceStatsByK).
      map { case (((k, records), (entropy, purity)), cds) =>
        if (records > 0) {
          f"| $k%5d | ${records}%10d | ${entropy}%10.8f | ${purity}%10.8f | ${cds.min}%12.4f | ${cds.avg}%12.4f | ${cds.max}%12.4f | ${cds.sse}%12.4f | "
        } else {
          f"| $k%5d | ${" "}%10s | ${" "}%10s | ${" "}%10s | ${" "}%12s | ${" "}%12s | ${" "}%12s | ${" "}%12s | "
        }

    }

    val resultsStr = "" :: "" ::
      "### Results Summary" :: "" ::
      f"| Results Info         | Value         |" ::
      f"| :------------------- | ------------: |" ::
      f"| WSSSE                | ${modelQI.modelDistanceStats.sse}%.7E |" ::
      f"| Min. Distance        | ${modelQI.modelDistanceStats.min}%13.11f |" ::
      f"| Avg. Distance        | ${modelQI.modelDistanceStats.avg}%13.11f |" ::
      f"| Max. Distance        | ${modelQI.modelDistanceStats.max}%13.11f |" ::
      f"| Average Entropy      | ${lcd.entropyByCluster.sum / params.k}%13.11f |" ::
      f"| Average Purity       | ${lcd.purityByCluster.sum / params.k}%13.11f |" ::
      f"| Training Runtime     | ${modelQI.trainingRuntime_ms / 1000 / 60}%02d:${modelQI.trainingRuntime_ms / 1000 % 60}%02d (mm:ss) |" ::
      Nil


    val legendStr = "" ::
      ("| Legend ||") ::
      ("| ------ | -------------------------------- |") ::
      ("| WSSSE  | Within Set Sum of Squared Errors |") ::
      ("| Clust  | Cluster Id                       |") ::
      Nil

    val summaryStr = title ++
      inputParamsStr ++
      resultsStr ++
      legendStr ++
      tabCountHeaderStr ++
      tabCountStr ++
      tabPercentageHeaderStr ++
      tabPercentagesStr ++
      tabCountByClustTitle ++
      tabCountByClustHeader ++
      tabCountByClustHeaderSeparator ++
      tabCountByClust ++
      tabCountByClustFooter ++
      tabPercentageByClustTitle ++
      tabCountByClustHeader ++
      tabCountByClustHeaderSeparator ++
      tabPercentageByClust ++
      tabCountByClustFooter ++
      tabClustInfoHeader ++
      tabClustInfo

    summaryStr

  }

}

object TrainKMeans {
  def apply() = new TrainKMeans
}


