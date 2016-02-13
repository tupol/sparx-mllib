package com.lunatic.mlx.kddcup99.mllib.metadata

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}

import scala.io.Source


case class KddRecord(data: Array[_]) {

  import KddRecord._

  val size: Int = data.length

  def label: Option[String] = if(isLabeled) Some(data(unlabeledSize).toString) else None

  def isLabeled: Boolean = size > unlabeledSize

  def get(index: Int) = data(index)

  /**
    * Convert the untyped data into an array of string and/or doubles
    *
    * @return
    */
  def toTypedRecord: KddRecord = {
    KddRecord((0 until columnsInfo.size).map { i =>
      val currentValue = data(i)
      if (columnsInfo(i)._2)
      // TODO This is not cool and can lead to bugs, so it needs to be fixed
        currentValue.toString.toDouble
      else
        currentValue.toString
    }.toArray)
  }

  override def toString: String = {
    data.mkString("[ ", ", ", " ]")
  }

  def toCsvString: String = {
    data.mkString(",")
  }
}

/**
  * Utility class for the KDD data.
  *
  * Holds KddRecord meta-data (column names and weather or not the features are continuous)
  *
  * Premises:
  *
  * [ ] The general assumption is that the label is the last column in the line array.
  *
  * [ ] There is no incomplete data
  */
object KddRecord {

  /**
    * The number of columns containing the unlabeled data
    */
  private val unlabeledSize = 41

  /**
    * This constant holds information about the columns in a sequence of tuples of column name and a boolean
    * indicating that it is a continuous feature or not.
    */
  val columnsInfo: Seq[(String, Boolean)] = Source.fromInputStream(KddRecord.getClass.getResourceAsStream("/kddcup99/kddcup.coltypes.txt"))
    .getLines
    .map(_.split(":")).map(arr => (arr(0).trim, arr(1).trim == "continuous.")).toSeq
    .take(unlabeledSize) // remove the label

  val columnLabels = columnsInfo.map(_._1)

  /**
    * Spark SQL Schema of the KDD Data
    */
  val schema = StructType {
    columnsInfo.map {
      _ match {
        case (cn, true) => StructField(cn, DoubleType, true)
        case (sn, false) => StructField(sn, StringType, true)
      }
    }
  }


  implicit def toRow(kddr: KddRecord): Row =
    Row.fromSeq(kddr.toTypedRecord.data)

  implicit def toRowRDD(rddr: RDD[KddRecord]): RDD[Row] =
    rddr.map(toRow)

  implicit def toKddRecord[T](data: Array[T]): KddRecord =
    KddRecord(data)

  implicit def toKddRecords[T](data: RDD[Array[T]]): RDD[KddRecord] =
    data.map(toKddRecord)

}
