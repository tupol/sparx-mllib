package com.lunatic.mlx.cuisines

import com.lunatic.mlx.cuisines.DaoUtils._
import com.lunatic.mlx.cuisines.mllib.{ClassMetrics, Model, MulticlassMetrix}
import com.lunatic.mlx.cuisines.model.PredictedRecipe
import org.apache.spark.SparkContext
import org.apache.spark.mllib.classification.{LogisticRegressionModel, NaiveBayesModel}
import org.apache.spark.mllib.tree.model.{DecisionTreeModel, RandomForestModel}
import org.elasticsearch.spark.rdd.EsSpark

/**
 *
 */
object ExportToES extends SparkRunnable {

  def main(args: Array[String]) = {

    DefaultSparkRunner(this.getClass.getName, args).run(this)

  }

  def run(implicit sc: SparkContext, configuration: Configuration) = {

    // Export the model metrics to ES
    val models: List[Model[_]] =
      List(
        LogisticRegressionModel.load(sc, configuration.logisticRegPath),
        NaiveBayesModel.load(sc, configuration.naiveBayesPath),
        DecisionTreeModel.load(sc, configuration.decisionTreePath),
        RandomForestModel.load(sc, configuration.randomForestPath)
      )
    case class ModelMetrics(name: String, metrics: MulticlassMetrix)
    val metrics = sc.parallelize(models.map(model => ModelMetrics(model.name, loadMetrix(model).get)))

    EsSpark.saveToEs(metrics,"cuisines/metrics")

    // Export the predictions to ES, one record / recipe
    val predictions = sc.objectFile[PredictedRecipe](configuration.outputPredictionsPath)

    EsSpark.saveToEs(predictions,"cuisines/predictions_raw")

    // Export predictions to ES flattened, one record / recipe / model
    case class Prediction(id: Int, ingredients: Seq[String], model: String, prediction: String, metrics: ClassMetrics)

    val exportPredictions = predictions.flatMap { pred =>
      pred.predictions.map{ p =>
        Prediction(pred.id, pred.ingredients, p.model, p.prediction, p.metrics)
      }
    }

    EsSpark.saveToEs(exportPredictions,"cuisines/predictions")

  }
}
