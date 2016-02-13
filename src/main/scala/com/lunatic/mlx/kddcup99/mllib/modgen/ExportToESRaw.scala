package com.lunatic.mlx.kddcup99.mllib.modgen

import com.lunatic.mlx.kddcup99.mllib.metadata.KddRecord
import KddRecord._
import com.lunatic.mlx.kddcup99.mllib.Configuration
import com.lunatic.mlx.kddcup99.mllib.metadata.KddRecord
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.elasticsearch.spark.sql._

/**
 * Save the Raw data to ES for analysis
 */
object ExportToESRaw extends SparkRunnable {

  def run(implicit sc: SparkContext, appConf: Configuration) = {

    val sqc = new SQLContext(sc)

    val lines: RDD[KddRecord] = sc.textFile(appConf.inputTrainingData).map(_.split(","))

    val dataDF = sqc.createDataFrame(lines, schema)

    dataDF.saveToEs(appConf.esIndexRoot + "/raw_input")
  }

}
