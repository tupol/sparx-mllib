package com.lunatic.mlx.kddcup99.mllib.thresholds

import com.lunatic.mlx.kddcup99.mllib.metadata.Prediction
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

/** top nth element by cluster as threshold */
case class NthAsThreshold(nthElement: Int)(implicit sc: SparkContext) extends AnomalyThresholdsGenerator {
  def generateThresholds(predictions: RDD[Prediction]): Array[Double] = {
    predictions
      .map { prediction => (prediction.cluster, prediction.distance) }
      .groupByKey().collect().sortBy(_._1)
      .map { stdDev => stdDev._2.toList.sorted.takeRight(nthElement).head }
  }
}
