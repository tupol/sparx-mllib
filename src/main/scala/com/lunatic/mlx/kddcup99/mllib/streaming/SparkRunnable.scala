package com.lunatic.mlx.kddcup99.mllib.streaming

import com.lunatic.mlx.kddcup99.mllib.Configuration
import org.apache.spark.streaming.StreamingContext

/**
 * Trivial trait for running basic Spark apps.
 *
 * The run() returns Unit, so just side effects... sorry
 */
trait SparkRunnable {

  def run(implicit ssc: StreamingContext, configuration: Configuration)

  def main(args: Array[String]) = {

    DefaultSparkRunner(this.getClass.getName, args).run(this)

  }
}
