package com.lunatic.mlx.kddcup99.mllib.metadata

/**
 * combinations of ranges to train KMeans model from (k clusters, max iterations, epsilon)
 * *range -> start, stop, step
 */
case class KMeansTrainingTemplate
(cluster_range: Seq[Int], iteration_range: Seq[Int], epsilon_range: Seq[Double]) {
  val initializationMode = "k-means||"
  val runs = 1
}
