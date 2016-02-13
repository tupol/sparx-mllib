package com.lunatic.mlx.kddcup99.mllib

import com.lunatic.mlx.kddcup99._
import com.lunatic.mlx.kddcup99.mllib.metadata.KMeansTrainingTemplate
import com.lunatic.mlx.kddcup99.mllib.transformers.DataNormalizer

import scala.util.Try

/**
  * Some basic configuration
  */
case class Configuration(args: Array[String]) {

  // TODO: Need I say more? Refactor and improve! A simple idea: configuration by usage (e.g. paths, ES, kmeans...)

  val appPrefix = "OT_KDD_"

  val splitArgs = args.map(_.split("="))

  // Inform the user about the missing argument values; maybe we should exit here... TBD
  splitArgs.filterNot(_.size == 2).foreach { x =>
    println(s"Application parameter '${x(0)}' is not defined; using defaults.")
  }

  // Retrieve only the properly defined pairs of arguments
  val argsMap = splitArgs.filter(_.size == 2).map { x => (x(0), x(1)) }.toMap


  //   val inputTrainingData = argsMap.getOrElse("app.input.file.training", "/tmp/demo-anod/data/kddcup.data")
  val inputTrainingData = argsMap.getOrElse("app.input.file.training", "/tmp/demo-anod/data/kddcup.data_10_percent")

  val inputTestingData = argsMap.getOrElse("app.input.file.test", "/tmp/demo-anod/data/kddcup.testdata.unlabeled")

  val workPath = argsMap.getOrElse("app.wip.path", "/tmp/demo-anod/wip")

  val outputPath = argsMap.getOrElse("app.output.path", "/tmp/demo-anod/out")

  val esIndexAutoCreate = argsMap.getOrElse("es.index.auto.create", "true")

  val esNodes = argsMap.getOrElse("es.nodes", "localhost")

  val esPort = argsMap.getOrElse("es.port", "9200")

  val esIndexRoot = argsMap.getOrElse("es.index", "kddcup99")

  val streamingBatchDurationSeconds = Try[Long](argsMap.get("app.streaming.batch.seconds").map(_.toLong).get).getOrElse(20L)

  val streamingCheckpoint = argsMap.getOrElse("app.streaming.checkpoint", "/tmp/")

  val streamingInputTrainingDir = argsMap.getOrElse("app.input.streaming.training", "/tmp/demo-anod/in/train")

  val streamingInputTestingDir = argsMap.getOrElse("app.input.streaming.testing", "/tmp/demo-anod/in/test")

  val trainDataSplitPath = workPath + "/data_train"

  val testDataSplitPath = workPath + "/data_test"

  val labelsCountPath = workPath + "/labels_count"

  val analyzerModelPath = workPath + "/analysis"

  private val clusterRange = argsMap.getOrElse("app.kmeans.clusterRange", "140,140,5")

  private val iterationRange = argsMap.getOrElse("app.kmeans.iterationRange", "1,1,1")

  private val epsilonRange = argsMap.getOrElse("app.kmeans.epsilonRange", "5,5,1")

  val kmeansTrainingTemplate = KMeansTrainingTemplate(parseStringToRange(clusterRange), parseStringToRange(iterationRange), parseStringToRange(epsilonRange).map(x => Math.pow(10, -x)))

  def transformerModelPath(name: String = DataNormalizer.L2NormV1): String = workPath + "/" + name

}

object Configuration {

  def preparedPath(path: String) = path + "_prepared"

}

