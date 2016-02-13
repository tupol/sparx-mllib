package com.lunatic.mlx.kddcup99.mllib.modgen

import com.lunatic.mlx.kddcup99._
import com.lunatic.mlx.kddcup99.mllib.Configuration
import com.lunatic.mlx.kddcup99.mllib.transformers.{DataNormalizer, KddTransformer}
import org.apache.spark.SparkContext


/**
 * Educate the KddTransformer for different normalization algorithms
 */
class EducateTransformers extends SparkRunnable {

  def run(implicit sc: SparkContext, appConf: Configuration) = {

    // The following are the index of the columns containing discrete data, including the label column
    // TODO Make this a configuration parameter
    val symboliColumns = List(1, 2, 3, 6, 11, 20, 21)

    // The input data analysis showd column 19 as having just one value, so we can remove it as we can learn nothing from it.
    // This also helps with the L2normalization since it is better not to dive by zero
    // TODO Make this a configuration parameter
    val removableColumns = List(19, 20)

    val labeledData = sc.textFile(appConf.inputTrainingData).map(_.split(",")).cache

    val unlabeledData = labeledData.map ( row => row.dropRight(1) )

    val transformer = KddTransformer(Some(symboliColumns), DataNormalizer.L2NormV1, Some(removableColumns)).learn(unlabeledData).asInstanceOf[KddTransformer]
    saveObjectToFile(transformer, appConf.transformerModelPath("KddTransformer"))

  }

}

object EducateTransformers {
  def apply() = new EducateTransformers
}

