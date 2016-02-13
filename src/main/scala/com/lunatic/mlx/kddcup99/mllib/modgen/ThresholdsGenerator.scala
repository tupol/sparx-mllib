package com.lunatic.mlx.kddcup99.mllib.modgen

import com.lunatic.mlx.kddcup99.mllib.metadata.{KMeansXMD, KMeansParams, KddRecord}
import KddRecord._
import com.lunatic.mlx.kddcup99.mllib.Configuration
import com.lunatic.mlx.kddcup99.mllib.thresholds._
import com.lunatic.mlx.kddcup99.{_}
import org.apache.spark.SparkContext
import org.apache.spark.annotation.Experimental

/**
 * Generate a series of threshold strategies for anomaly detection,
 * presently we've computed the cutoff based for:
 * StandardDeviation
 */
@Experimental
class ThresholdsGenerator extends SparkRunnable {

  def run(implicit sc: SparkContext, appConf: Configuration) {
    generateAnomalyThresholds
  }

  def generateAnomalyThresholds(implicit sc: SparkContext, appConf: Configuration) {

    val k_indices = appConf.kmeansTrainingTemplate.cluster_range
    val iteration_indices = appConf.kmeansTrainingTemplate.iteration_range
    val epsilon_indices = appConf.kmeansTrainingTemplate.epsilon_range
    val runs = appConf.kmeansTrainingTemplate.runs
    val file = appConf.trainDataSplitPath

    for (kValue <- k_indices;
         iterations <- iteration_indices;
         epsilon <- epsilon_indices
    ) yield {

      val params = KMeansParams(kValue, epsilon, iterations, runs)
      val modelFile = stubFilePath(params) + ".model"
      val kMeansXMD = loadObjectFromFile[KMeansXMD](modelFile + ".xmd").get
      val rawTrainData = sc.objectFile[Array[String]](file)

      val kddRecords = rawTrainData.map(toKddRecord)

      val predictions = PredictKMeans.predict(kMeansXMD, kddRecords)

      // todo: this should become an application param
      val nthElement = 5
      val nthElementAvgTop10 = 10
      val nthElementStdDevTop100 = 100

      // todo: replace spark context computations with plain scala
      /** anomaly detection strategy templates */
      val generators = Seq(
        StdDeviationThreshold(),
        TopAverageElements(nthElementAvgTop10),
        StdDeviationTop(nthElementStdDevTop100),
        ConstantThreshold(kMeansXMD, 3.0),
        NthAsThreshold(nthElement)
      )

      val newModel = generators
        .foldLeft(kMeansXMD)
        { (acc, current) =>
          acc.addAnomalyThreshold(current.getClass.getSimpleName, current.generateThresholds(predictions))
        }

      // overwriting old model with a new model containing anomaly thresholds
      saveObjectToFile(newModel, modelFile + ".xmd")
    }

  }

}

object ThresholdsGenerator {
  def apply() = new ThresholdsGenerator
}
