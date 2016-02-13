package com.lunatic.mlx.kddcup99.mllib.modgen

import com.lunatic.mlx.kddcup99._
import com.lunatic.mlx.kddcup99.mllib.metadata._
import AnalysedPrediction._
import com.lunatic.mlx.kddcup99.mllib.Configuration
import org.apache.spark.SparkContext
import org.apache.spark.annotation.Experimental
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.elasticsearch.spark.sql._

import scala.util.Try

/**
 * For now this is not a true predictor, but rather a re-evaluator for the existing model; useful
 * for adding more meaningful information about the model in the output (.results_1)
 */
@Experimental
class PredictKMeans extends SparkRunnable {

  import PredictKMeans._

  def run(implicit sc: SparkContext, appConf: Configuration) = {
    predict
    //predictAndSaveSmallSample
  }


}

object PredictKMeans {
  def apply() = new PredictKMeans

  def predict(implicit sc: SparkContext, appConf: Configuration): Unit = {

    val k_indices = appConf.kmeansTrainingTemplate.cluster_range
    val iteration_indices = appConf.kmeansTrainingTemplate.iteration_range
    val epsilon_indices = appConf.kmeansTrainingTemplate.epsilon_range
    val runs = appConf.kmeansTrainingTemplate.runs
    val files = Seq((appConf.trainDataSplitPath, "train"), (appConf.testDataSplitPath, "test"))

    for (file <- files;
         kValue <- k_indices;
         iterations <- iteration_indices;
         epsilon <- epsilon_indices
    ) yield {

      val params = KMeansParams(kValue, epsilon, iterations, runs)
      val modelFile = stubFilePath(params) + ".model"
      val kMeansXMD = loadObjectFromFile[KMeansXMD](modelFile + ".xmd").get

      val filePath = file._1
      val fileLabel = file._2

      val rawData = sc.objectFile[Array[String]](filePath)

      val predictions = predict(kMeansXMD, rawData)

      val detections = detectAnomalies(predictions, kMeansXMD.anomalyThresholds)

      val results = toOutputLines(detections, kMeansXMD.anomalyThresholds)

      val predFile = stubFilePath(params) + s"_${fileLabel}.prediction"
      saveLinesToFile(results, predFile)

      // Try saving also to ES
      val sqc = new SQLContext(sc)
      val schema = AnalysedPrediction.schema(kMeansXMD.anomalyThresholds.map(_._1))
      val dataDF = sqc.createDataFrame(detections, schema)
      Try(dataDF.saveToEs(appConf.esIndexRoot + "/predictions_stream"))
    }

  }

  def predict(mx: KMeansXMD, rdd: RDD[KddRecord])
             (implicit sc: SparkContext, appConf: Configuration): RDD[Prediction] = {

    rdd.map { kddr =>
      val vector = mx.preProcessor.transform(kddr.data.map(_.toString))
      val model = mx.model
      val k = model.predict(vector)
      val distance = Vectors.sqdist(vector, model.clusterCenters(k))

      Prediction(k, distance, mx.id, kddr.data, kddr.label.getOrElse("???"))

    }
  }

  /**
    * Detect which record is an anomaly based on the given threshold (per cluster)
    *
    * @param rdd
    * @param anomalyThresholds
    * @return
    */
  def detectAnomalies(rdd: RDD[Prediction], anomalyThresholds: Seq[(String, Seq[Double])]): RDD[AnalysedPrediction] = {
    rdd.map{ prediction =>
      val k = prediction.cluster
      val distance = prediction.distance

      val anomalyByStrategy = anomalyThresholds.map { case (name, thresholds) =>
        val threshold = thresholds(k)
        (name, distance > threshold)
      }
      AnalysedPrediction(anomalyByStrategy, prediction)
    }
  }


  /**
    * Print some details about anomalies detected
    *
    * @param apreds
    * @param anomalyThresholds
    * @return
    */
  def toOutputLines(apreds: RDD[AnalysedPrediction], anomalyThresholds: Seq[(String, Seq[Double])]) = {

    val totalPredictions = apreds.count()

    // We consider it an anomaly if any of the thresholds we applied yield true (anomaly)
    val anomalies = apreds.filter { case (apred)  =>
      apred.anomalyPredictions.map(_._2).foldLeft(false)((accu, ano) => accu || ano)
    }

    val totalAno = anomalies.count()

    val anomaliesByStrategy = anomalies
      .flatMap(x => x.anomalyPredictions).groupByKey()
      .map(pair => (pair._1, pair._2.count(_ == true))).collect().sortBy(_._1).toSeq

    val summary = "" +:
      f"Distance Thresholds: " +:
      f"${anomalyThresholds.map{ case(name, thresholds) =>
        val thrsStr = thresholds.mkString(", ")
        f"    ${name}%-28s : ${thrsStr}" }.
        mkString("\n")}" +:
      f"Total predictions:   $totalPredictions" +:
      f"Total anomalies:     $totalAno" +:
      f"Anomalies By Strategy:     " +:
      anomaliesByStrategy.map {
        pair => f"| ${pair._1}%-28s | ${pair._2}%8d |"
      }.mkString("\n") +:
      "" +: Nil
    val anomaliesHeader = anomalyThresholds.map { case (name, thrs) => f"${name}%-28s" }.mkString(" | ")
    val header =
      f"| ${anomaliesHeader} | ${"Timestamp"}%-28s | ${"Label"}%-20s | ${"Clust"}%-5s | ${"Distance"}%-12s | ${"Model Id"}%-30s | Input Vector" +:
        Nil
    val table =
      anomalies.map { case (apred)  =>
        val anoData = apred.anomalyPredictions.map{ case(name, ano) =>
          if(ano) f"${"ANOMALY"}%-28s" else f"${" "}%-28s"
        }.mkString(" | ")
        val prediction = apred.prediction
        f"| ${anoData} | ${prediction.timestamp}%-28s | ${prediction.label}%-20s | ${prediction.cluster}%5d | ${prediction.distance}%12.6f | ${prediction.modelId}%-30s | ${prediction.kddr.data.mkString(", ")} |"

      }.collect.toSeq

    summary ++ header ++ table
  }

}
