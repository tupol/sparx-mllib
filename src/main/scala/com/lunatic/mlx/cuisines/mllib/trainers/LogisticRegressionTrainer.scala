package com.lunatic.mlx.cuisines.mllib.trainers

import com.lunatic.mlx.cuisines.mllib.FlowData
import com.lunatic.mlx.cuisines.{Configuration, printEvaluationMetrix}
import com.lunatic.mlx.removeHdfsFile
import org.apache.spark.mllib.classification.{LogisticRegressionModel, LogisticRegressionWithLBFGS}
import org.apache.spark.{SparkConf, SparkContext}

/**
 *
 */
class LogisticRegressionTrainer() extends Trainer[LogisticRegressionModel] {

  def train(flowData: FlowData)(implicit sc: SparkContext) = {

    val numClasses = flowData.labelToIndex.size + 1

    val trainingData = flowData.data

    new LogisticRegressionWithLBFGS()
      .setNumClasses(numClasses)
      .run(trainingData)
  }

}

object LogisticRegressionTrainer {

  def apply() = new LogisticRegressionTrainer

  def main(args: Array[String]) = {

    val conf = new SparkConf(true).setAppName(this.getClass.getSimpleName).
      setMaster("local[*]")

    implicit val sc = new SparkContext(conf)
    implicit val configuration = Configuration(args)

    val flowData = FlowData.load(configuration.dataPath)

    val (model, metrics) = LogisticRegressionTrainer().trainEvaluate(flowData)

    removeHdfsFile(configuration.logisticRegPath)
    model.save(configuration.logisticRegPath)

    println(s"### ${model.self.getClass.getSimpleName} model evaluation")

    printEvaluationMetrix(metrics)

  }

}
