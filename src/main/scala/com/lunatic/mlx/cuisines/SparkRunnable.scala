package com.lunatic.mlx.cuisines

import org.apache.spark.SparkContext

/**
 * Trivial trait for running basic Spark apps.
 *
 * The run() returns Unit, so just side effects... sorry
 */
trait SparkRunnable {

  def run(implicit sc: SparkContext, configuration: Configuration)

}
