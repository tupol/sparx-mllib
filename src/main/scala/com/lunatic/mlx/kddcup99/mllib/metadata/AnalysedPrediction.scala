package com.lunatic.mlx.kddcup99.mllib.metadata

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

/**
  * This is an evolution of the Prediction class, which contains the results of the anomaly detection analysis,
  * for each threshold strategy chosen.
  */
case class AnalysedPrediction(anomalyPredictions: Seq[(String, Boolean)], prediction: Prediction) {

  override def toString(): String = {
    val anomaliesStr = anomalyPredictions.map { case (name, isAnomaly) =>
      if(isAnomaly) f"${"ANOMALY"}%-28s" else f"${" "}%-28s"
    }.mkString("| ", " | ", " ")
    anomaliesStr + prediction.toString
  }

  // TODO move this outside in a decorator
  def toCsv(): String = {
    anomalyPredictions.map(_._2).mkString(",") + prediction.toCsv()
  }

}

/**
  * Prediction class companion
  */
object AnalysedPrediction {

  implicit def toRow(pred: AnalysedPrediction): Row = {
    Row.fromSeq(pred.anomalyPredictions.map(_._2) ++ Prediction.toRow(pred.prediction).toSeq)

  }

  implicit def toRowRDD(rdd: RDD[AnalysedPrediction]): RDD[Row] = {
    rdd.map(toRow(_))
  }

  def schema(anomalyPredictions: Seq[String]) =  StructType {
    val resultStruct =
      anomalyPredictions.map { case (name) =>
        StructField(name, BooleanType, true)
      }
    resultStruct ++ Prediction.schema.fields
  }

}
