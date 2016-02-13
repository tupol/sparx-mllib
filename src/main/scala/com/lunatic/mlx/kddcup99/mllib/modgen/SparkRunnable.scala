package com.lunatic.mlx.kddcup99.mllib.modgen

import com.lunatic.mlx.kddcup99.mllib.Configuration
import org.apache.spark.SparkContext

/**
 * Trivial trait for running basic Spark apps.
 *
 * The run() returns Unit, so just side effects... sorry
 */
trait SparkRunnable {

  def run(implicit sc: SparkContext, configuration: Configuration)

  def main(args: Array[String]) = {

    DefaultSparkRunner(this.getClass.getName, args).run(this)

  }
}
