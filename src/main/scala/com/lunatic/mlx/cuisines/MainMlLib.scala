package com.lunatic.mlx.cuisines

import org.apache.spark.{SparkContext, SparkConf}

/**
 *
 */
object MainMlLib {

  def main(args: Array[String]) = {

    implicit val configuration = Configuration(args)

    val defConf = new SparkConf(true)
    val conf = defConf.setAppName("CuisineRecipesImportData").
      setMaster(defConf.get("spark.master",  "local[*]")).
      set("es.index.auto.create", configuration.es_index_auto_create).
      set("es.nodes", configuration.es_nodes).
      set("es.port", configuration.es_port)

    implicit val sc = new SparkContext(conf)

    run
  }

  def run(implicit sc: SparkContext, configuration: Configuration) = {

    val runners = List(
      ImportData,
      BuildModels,
      BuildPredictions,
      ExportToCSV,
      ExportToES)

    runners.foreach(_.run)
  }

}
