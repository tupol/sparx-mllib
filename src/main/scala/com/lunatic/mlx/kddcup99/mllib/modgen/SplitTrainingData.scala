package com.lunatic.mlx.kddcup99.mllib.modgen

import com.lunatic.mlx.kddcup99._
import com.lunatic.mlx.kddcup99.mllib.Configuration
import com.lunatic.mlx.kddcup99.mllib.transformers.InputAnalyzer
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import scala.reflect.ClassTag


/**
  * Prepare training data: split into training and test by given ratio
  * (trying to get the same ratio for each label, just to keep things in balance)
  * and normalize the data by the known normalization algorithms.
  *
  */
class SplitTrainingData extends SparkRunnable {

  import SplitTrainingData._

  def run(implicit sc: SparkContext, appConf: Configuration) = {

    /**
      * Value between 0 and 1, representing the training part; recommended between 0.6 and 0.8
      */
    val splitRatio = 0.7

    val labelColumn = 41

    val analyser = loadObjectFromFile[InputAnalyzer[String]](appConf.analyzerModelPath).get

    //TODO Ugly!!! Redo!
    val labelsCount = analyser.distinctCountByColumn.get.get(labelColumn).get

    val rawData = sc.textFile(appConf.inputTrainingData).map(_.split(",")).cache

    val distinctLabels = labelsCount.map(_._1).toSeq

    val (trainData, testData) = splitEvenlyByLabel(splitRatio, labelColumn, distinctLabels, rawData)

    // Save un-normalized training set
    removeHdfsFile(appConf.trainDataSplitPath)
    trainData.saveAsObjectFile(appConf.trainDataSplitPath)

    // Save un-normalized test set
    removeHdfsFile(appConf.testDataSplitPath)
    testData.saveAsObjectFile(appConf.testDataSplitPath)


  }


}

object SplitTrainingData {

  def apply() = new SplitTrainingData

  def splitEvenlyByLabel[T : ClassTag](ratio: Double, labelColumn: Int, data: RDD[Array[T]]):
  (RDD[Array[T]], RDD[Array[T]]) = {

    val colsSize = data.first.length
    require(labelColumn >= 0 && labelColumn < colsSize, s"Label column must be in range 0 to $colsSize")

    val distinctLabels = data.map(row => row(labelColumn)).distinct.collect

    splitEvenlyByLabel(ratio, labelColumn, distinctLabels, data)
  }

  /**
    * Split the data by the given ratio for each label
 *
    * @param ratio the ratio of the first element of the tuple result
    * @param labelColumn which column is the one holding the labels
    * @param distinctLabels if we already have this, better use it
    * @param data
    * @return
    */
  def splitEvenlyByLabel[T : ClassTag](ratio: Double, labelColumn: Int, distinctLabels: Seq[T], data: RDD[Array[T]]):
  (RDD[Array[T]], RDD[Array[T]]) = {

    val colsSize = data.first.length
    require(labelColumn >= 0 && labelColumn < colsSize, s"Label column must be in range 0 to $colsSize")

    val distinctLabels = data.map(row => row(labelColumn)).distinct.collect

    // TODO This is highly unoptimized :) so it should be optimized
    distinctLabels.map { label =>
      val splits = data.filter(row => row(labelColumn) == label).randomSplit(Array(ratio, 1-ratio))
      if(splits.size == 1)
        (splits(0), splits(0))
      else
        (splits(0), splits(1))
    }.reduce { (x1, x2) => (x1._1.union(x2._1), x1._2.union(x2._2)) }
  }

}
