package com.lunatic.mlx.kddcup99.mllib.thresholds

import com.lunatic.mlx.kddcup99.mllib.metadata.Prediction
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

/** standard deviation for the top n elements */
case class StdDeviationTop(nthElement: Int)(implicit sc: SparkContext) extends AnomalyThresholdsGenerator {
  def generateThresholds(predictions: RDD[Prediction]): Array[Double] = {
    predictions
      .map { prediction => (prediction.cluster, prediction.distance) }
      .groupByKey().collect().sortBy(_._1)
      .map { stdDev => sc.parallelize(stdDev._2.toList.sorted.takeRight(nthElement)) }
      .map { clusterCutoff => clusterCutoff.mean() + clusterCutoff.stdev() }
  }
}
