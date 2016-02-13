package com.lunatic.mlx.kddcup99.mllib.modgen

import com.lunatic.mlx.kddcup99._
import com.lunatic.mlx.kddcup99.mllib.Configuration
import Configuration._
import com.lunatic.mlx.kddcup99.mllib.transformers.KddTransformer
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD


/**
  * Prepare training data: split into training and test by given ratio
  * (trying to get the same ratio for each label, just to keep things in balance)
  * and normalize the data by the known normalization algorithms.
  *
  */
class PreProcessData extends SparkRunnable {

  def run(implicit sc: SparkContext, appConf: Configuration) = {

    val files = List(appConf.trainDataSplitPath, appConf.testDataSplitPath)

    val transformer = loadObjectFromFile[KddTransformer](appConf.transformerModelPath("KddTransformer"))

    for(
      file <- files
    ) yield {
      // Load un-normalized training set
      val labeledData = sc.objectFile[Array[String]](file)
      //Normalize and save training set;
      val preparedData = prepareData(labeledData, transformer.get)
      val outFile = preparedPath(file)
      removeHdfsFile(outFile)
      preparedData.saveAsObjectFile(outFile)
    }

  }

  /**
    * Normalize data and preserve labels
 *
    * @param data
    * @param transformer
    * @return
    */
  def prepareData(data: RDD[Array[String]], transformer: KddTransformer) = {

    val unlabeledData = data.map { row =>
      row.dropRight(1)
    }
    val labels = data.map(_.takeRight(1)(0))

    val normData = transformer.transform(unlabeledData)

    labels zip normData
  }

}

object PreProcessData {
  def apply() = new PreProcessData
}
