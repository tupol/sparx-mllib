package com.lunatic.mlx.cuisines.mllib.trainers

import com.lunatic.mlx.cuisines.mllib.{FlowData, Model}
import com.lunatic.mlx.cuisines.{Configuration, printEvaluationMetrix}
import com.lunatic.mlx.removeHdfsFile
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.{SparkConf, SparkContext}

/**
 *
 * @param maxDepth
 * @param maxBins
 * @param impurity acceptable values: "gini" or "entropy"
 */
class DecisionTreeTrainer(maxDepth: Int = 15,
                          maxBins: Int = 500,
                          impurity: String = "gini")
  extends Trainer[DecisionTreeModel] {

  def train(flowData: FlowData)(implicit sc: SparkContext): Model[DecisionTreeModel] = {

    val numClasses = flowData.labelToIndex.size + 1
    val numFeatures = flowData.featureToIndex.size

    val trainingData = flowData.data

    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    val categoricalFeaturesInfo = (0 until numFeatures).map(i => i -> 2).toMap

    DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
      impurity, maxDepth, maxBins)
  }

}

object DecisionTreeTrainer {

  def apply() = new DecisionTreeTrainer

  def main(args: Array[String]) = {

    val conf = new SparkConf(true).setAppName(this.getClass.getSimpleName).
      setMaster("local[*]")

    implicit val sc = new SparkContext(conf)
    implicit val configuration = Configuration(args)

    val flowData = FlowData.load(configuration.dataPath)

    val (model, metrics) = DecisionTreeTrainer().trainEvaluate(flowData)

    removeHdfsFile(configuration.decisionTreePath)
    model.save(configuration.decisionTreePath)

    println(s"### ${model.self.getClass.getSimpleName} model evaluation")

    printEvaluationMetrix(metrics)

  }

}
