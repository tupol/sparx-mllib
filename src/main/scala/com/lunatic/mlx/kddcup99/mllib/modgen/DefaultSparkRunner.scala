package com.lunatic.mlx.kddcup99.mllib.modgen

import com.lunatic.mlx.kddcup99.mllib.Configuration
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Convenience object that can run any given SparkRunner
 */
case class DefaultSparkRunner(runnerName: String, args: Array[String]) {

  implicit val appConf = Configuration(args)

  val defSparkConf = new SparkConf(true)
  val sparkConf = defSparkConf.setAppName(appConf.appPrefix + runnerName).
    setMaster(defSparkConf.get("spark.master", "local[*]")).
    set("es.index.auto.create", appConf.esIndexAutoCreate).
    set("es.nodes", appConf.esNodes).
    set("es.port", appConf.esPort)

  implicit val sc = new SparkContext(sparkConf)

  def run(runnable: SparkRunnable): Unit = runnable.run

  def run(runnables: Seq[SparkRunnable]): Unit = runnables.foreach(_.run)


}
