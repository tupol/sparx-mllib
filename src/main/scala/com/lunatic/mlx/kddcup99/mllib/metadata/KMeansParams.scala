package com.lunatic.mlx.kddcup99.mllib.metadata

import com.lunatic.mlx.kddcup99.mllib.transformers.DataNormalizer

/**
  * Small configuration for the KMeans algorithm
  *
  * @param k
  * @param epsilon
  * @param maxIterations
  * @param runs
  * @param norm @see ImportData
  */
case class KMeansParams(k: Int, epsilon: Double, maxIterations: Int, runs: Int, norm: String = DataNormalizer.L2NormV1) {

  /**
    * Generate an experiment if based on the experiment parameters
 *
    * @return
    */
  def experimentId: String =
    KMeansParams.experimentId(k, epsilon, maxIterations, runs, norm)

}

object KMeansParams {

  /**
    * Generate an experiment if based on the experiment parameters
    *
    * @param k
    * @param epsilon
    * @param maxIterations
    * @param runs
    * @param norm Just an arbitrary string suggesting the normalization algorithm used
    * @return experimentId
    */
  def experimentId(k: Int, epsilon: Double, maxIterations: Int, runs: Int, norm: String): String =
    f"$epsilon%.0E_$k%03d_$maxIterations%04d_$runs%02d_$norm".
      replaceAll("\\+", "")

}
