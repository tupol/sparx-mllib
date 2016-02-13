package com.lunatic.mlx.kddcup99.mllib.metadata

import java.time.Instant

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._


/**
  * Prediction data
  *
  * @param cluster predicted cluster
  * @param distance distance between this point and cluster centroid
  * @param modelId prediction model identifier
  * @param kddr input data for prediction
  * @param label label (if available
  * @param timestamp time of the prediction
  */
case class Prediction(cluster: Int, distance: Double = -1.0, modelId: String, kddr: KddRecord, label: String = "???", timestamp: Instant = Instant.now) {

  // TODO Make label Option[String]

  override def toString(): String = {
    val ds = kddr.mkString("[ ", ", ", " ]")
    f"| $timestamp%-28s| $label%-20s | $cluster%3d | $distance%9.4f | $modelId%-20s | $ds |"
  }

  // TODO move this outside in a decorator
  def toCsv(): String = {
    s"$timestamp,$label,$cluster,$distance,$modelId,${kddr.toCsvString}"
  }

}


/**
  * Prediction class companion
  */
object Prediction {

  implicit def toRow(pred: Prediction): Row = {
    Row.fromSeq(
      (List(pred.timestamp.toString, pred.label, pred.cluster, pred.distance, pred.modelId) ++
      pred.kddr.toTypedRecord.data)
    )
  }

  implicit def toRowRDD(rdd: RDD[Prediction]): RDD[Row] = {
    rdd.map(toRow(_))
  }

  val schema =  StructType {
      val resultStruct =
        // TODO make timestamp a proper date/time field
        StructField("timestamp", StringType, false) ::
        StructField("label", StringType, true) ::
        StructField("cluster", IntegerType, false) ::
        StructField("distance", DoubleType, false) ::
        StructField("modelId", StringType, false) ::
      Nil

      resultStruct ++ KddRecord.schema.fields
  }

}


