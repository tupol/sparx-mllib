package com.lunatic.mlx.cuisines.mllib

import com.lunatic.mlx.cuisines.Configuration
import com.lunatic.mlx.removeHdfsFile
import org.apache.spark.SparkContext
import org.apache.spark.mllib.classification.{LogisticRegressionModel, NaiveBayesModel}
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.tree.model.{DecisionTreeModel, RandomForestModel}
import org.apache.spark.rdd.RDD

/**
 * Interface unification for the MLLib models
 * @tparam T
 */
trait Model[T] extends Serializable {
  def self: T

  def predict(testData: Vector): Double

  def predict(testData: RDD[Vector]): RDD[Double]

  def save(path: String)(implicit sc: SparkContext, configuration: Configuration): Unit

  def name = self.getClass.getSimpleName
}


object Model {

  implicit def classificationModelToModel(originalModel: LogisticRegressionModel) =
    new Model[LogisticRegressionModel] {
      def self = originalModel

      def predict(testData: Vector): Double = {
        originalModel.predict(testData)
      }

      def predict(testData: RDD[Vector]): RDD[Double] = {
        originalModel.predict(testData)
      }

      def save(path: String)(implicit sc: SparkContext, configuration: Configuration) = {
        removeHdfsFile(path)
        originalModel.save(sc, path)
      }
    }

  implicit def naiveBayesModelToModel(originalModel: NaiveBayesModel) =
    new Model[NaiveBayesModel] {
      def self = originalModel

      def predict(testData: Vector): Double = {
        originalModel.predict(testData)
      }

      def predict(testData: RDD[Vector]): RDD[Double] = {
        originalModel.predict(testData)
      }

      def save(path: String)(implicit sc: SparkContext, configuration: Configuration) = {
        removeHdfsFile(path)
        originalModel.save(sc, path)
      }
    }


  implicit def decisionTreeModelToModel(originalModel: DecisionTreeModel) =
    new Model[DecisionTreeModel] {
      def self = originalModel

      def predict(testData: Vector): Double = {
        originalModel.predict(testData)
      }

      def predict(testData: RDD[Vector]): RDD[Double] = {
        originalModel.predict(testData)
      }

      def save(path: String)(implicit sc: SparkContext, configuration: Configuration) = {
        removeHdfsFile(path)
        originalModel.save(sc, path)
      }
    }


  implicit def randomForestModelToModel(originalModel: RandomForestModel) =
    new Model[RandomForestModel] {
      def self = originalModel

      def predict(testData: Vector): Double = {
        originalModel.predict(testData)
      }

      def predict(testData: RDD[Vector]): RDD[Double] = {
        originalModel.predict(testData)
      }

      def save(path: String)(implicit sc: SparkContext, configuration: Configuration) = {
        removeHdfsFile(path)
        originalModel.save(sc, path)
      }
    }
}


