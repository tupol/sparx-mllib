package com.lunatic.mlx.kddcup99.mllib.streaming

import com.lunatic.mlx.kddcup99.mllib.Configuration
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Duration, Seconds, StreamingContext}

/**
 * Convenience object that can run any given SparkRunnable
 */
case class DefaultSparkRunner(runnerName: String, args: Array[String], batchDuration: Duration = Seconds(20)) {

  implicit val appConf = Configuration(args)

  implicit val ssc = createStreamingContext

  def run(runnable: SparkRunnable): Unit = runnable.run

  def run(runnables: Seq[SparkRunnable]): Unit = runnables.foreach(_.run)


  private def createStreamingContext(checkpoint: String)(implicit appConf: Configuration): StreamingContext = {
    val defSparkConf = new SparkConf(true)
    val sparkConf = defSparkConf.setAppName(appConf.appPrefix + runnerName).
      setMaster(defSparkConf.get("spark.master",  "local[*]")).
      set("spark.streaming.stopGracefullyOnShutdown", "true").
      set("es.index.auto.create", appConf.esIndexAutoCreate).
      set("es.nodes", appConf.esNodes).
      set("es.port", appConf.esPort)

    val ssc = new StreamingContext(sparkConf, Seconds(appConf.streamingBatchDurationSeconds))

    ssc.checkpoint(checkpoint)

    ssc
  }

  private def createStreamingContext(implicit appConf: Configuration): StreamingContext = {
    val defSparkConf = new SparkConf(true)
    val sparkConf = defSparkConf.setAppName(appConf.appPrefix + runnerName).
      setMaster(defSparkConf.get("spark.master",  "local[*]")).
      set("spark.streaming.stopGracefullyOnShutdown", "true").
      set("es.index.auto.create", appConf.esIndexAutoCreate).
      set("es.nodes", appConf.esNodes).
      set("es.port", appConf.esPort)

    val ssc = new StreamingContext(sparkConf, batchDuration)

    ssc
  }

}
