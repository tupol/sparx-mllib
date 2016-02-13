package com.lunatic.mlx.kddcup99.mllib.thresholds

import com.lunatic.mlx.kddcup99.mllib.metadata.Prediction
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

/** standard deviation threshold */
case class StdDeviationThreshold(implicit sc: SparkContext) extends AnomalyThresholdsGenerator {
  def generateThresholds(predictions: RDD[Prediction]): Array[Double] = {
    predictions
      .map { prediction => (prediction.cluster, prediction.distance) }
      .groupByKey().sortByKey().collect()
      .map { stdDev => sc.parallelize(stdDev._2.toList) }
      .map { clusterCutoff => clusterCutoff.mean() + clusterCutoff.stdev() }
  }
}
