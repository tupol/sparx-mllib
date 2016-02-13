package com.lunatic.mlx.cuisines

import com.lunatic.mlx.cuisines.mllib.{Model, MulticlassMetrix}
import com.lunatic.mlx.removeHdfsFile
import org.apache.spark.SparkContext
import org.apache.spark.mllib.classification.{LogisticRegressionModel, NaiveBayesModel}
import org.apache.spark.mllib.tree.model.{DecisionTreeModel, RandomForestModel}

/**
 * VERY UGLY, but for now it works.
 *
 * This might be a good type classes implementation idea.
 */
object DaoUtils {

  def saveMetrix(model: Model[_], metrics: MulticlassMetrix)(implicit sc: SparkContext, configuration: Configuration) = {
    val path = getPath(model) + ".metrics"
    removeHdfsFile(path)
    sc.parallelize(Seq(metrics)).saveAsObjectFile(path)
  }

  def loadMetrix(model: Model[_])(implicit sc: SparkContext, configuration: Configuration):
  Option[MulticlassMetrix] = {
    val path = getPath(model) + ".metrics"
    sc.objectFile[MulticlassMetrix](path).collect.toSeq.headOption
  }

  def getPath(model: Model[_])(implicit configuration: Configuration) = model.self match {
    case m1: LogisticRegressionModel => configuration.logisticRegPath
    case m2: NaiveBayesModel => configuration.naiveBayesPath
    case m2: DecisionTreeModel => configuration.decisionTreePath
    case m2: RandomForestModel => configuration.randomForestPath
  }

}
