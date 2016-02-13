package com.lunatic.mlx.kddcup99.mllib.thresholds

import com.lunatic.mlx.kddcup99.mllib.metadata.{Prediction, KMeansXMD}
import org.apache.spark.rdd.RDD

/** constant threshold initially used for comparison */
case class ConstantThreshold(kMeansXMD: KMeansXMD, threshold: Double) extends AnomalyThresholdsGenerator {
  def generateThresholds(predictions: RDD[Prediction]): Array[Double] =
    kMeansXMD.model.clusterCenters.map(_ => threshold)
}
