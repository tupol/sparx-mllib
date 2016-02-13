package com.lunatic.mlx.kddcup99.mllib.modgen

import com.lunatic.mlx.kddcup99._
import com.lunatic.mlx.kddcup99.mllib.Configuration
import com.lunatic.mlx.kddcup99.mllib.transformers.InputAnalyzer
import org.apache.spark.SparkContext

import scala.io.Source


/**
  * Analyse the input data and provide suggestions for columns to be removed.
  *
  * This is very expensive, so it should be ran once per input training data.
  */
class AnalyseInputData extends SparkRunnable {

  def run(implicit sc: SparkContext, appConf: Configuration) = {

    val labeledData = sc.textFile(appConf.inputTrainingData).map(_.split(",")).cache

    val colTypes = Source.fromInputStream(EducateTransformers.getClass.getResourceAsStream("/kddcup99/kddcup.coltypes.txt"))
      .getLines
      .map(_.split(":")).map(arr => (arr(0).trim, arr(1).trim == "continuous."))

    val colsDict = colTypes.map(_._1).zipWithIndex.map(_.swap).toMap

    val analyzer = new InputAnalyzer[String](None, Some(colsDict)).learn(labeledData).asInstanceOf[InputAnalyzer[String]]
    saveObjectToFile(analyzer, appConf.analyzerModelPath)

    val analisysResult = analyzer.analysisReport(labeledData)
    saveLinesToFile(analisysResult, appConf.outputPath + "/analysis.txt")
    println(analisysResult.mkString("\n"))

  }

}

object AnalyseInputData {
  def apply() = new AnalyseInputData
}
