package com.lunatic.mlx.cuisines

import com.lunatic.mlx.cuisines.mllib.trainers._
import com.lunatic.mlx.cuisines.mllib.{FlowData, Model, MulticlassMetrix}
import com.lunatic.mlx.removeHdfsFile
import org.apache.spark.SparkContext

/**
  * Build the model for the given Trainers and save the models
  */
object BuildModels extends SparkRunnable {

  def main(args: Array[String]) = {

    DefaultSparkRunner(this.getClass.getName, args).run(this)

  }

  def run(implicit sc: SparkContext, configuration: Configuration) = {

    import DaoUtils._

    // Load the flow data
    val flowData = FlowData.load(configuration.dataPath)

    def train[T](trainer: Trainer[T]): (Model[T], MulticlassMetrix) =
      trainer.trainEvaluate(flowData)

    val trainers = List(
      LogisticRegressionTrainer(),
      NaiveBayesTrainer(),
      DecisionTreeTrainer(),
      RandomForestTrainer()
    )

    val trainingResults = trainers.map(train(_))

    // Train the models and save the models for later use
    trainingResults.map { case (model, metrics) =>
        val path = DaoUtils.getPath(model)
        removeHdfsFile(path)
        model.save(path)
    }

    // Print the models evaluation (GitHub friendly)
    trainingResults.foreach { case (model, metrics) =>
      println(s"\n### ${model.name} model evaluation")
      printEvaluationMetrix(metrics)
      saveMetrix(model, metrics)
    }

  }

}
