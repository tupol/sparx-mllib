package com.lunatic.mlx.kddcup99.mllib.thresholds

import com.lunatic.mlx.kddcup99.mllib.metadata.Prediction
import org.apache.spark.rdd.RDD

trait AnomalyThresholdsGenerator {
  def generateThresholds(predictions: RDD[Prediction]): Array[Double]
}
