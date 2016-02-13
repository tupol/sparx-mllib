package com.lunatic.mlx.kddcup99

import org.apache.spark.rdd.RDD

/**
  *
  */
package object ml {


  /**
    * Split the data RDD into two parts, first part corresponding to the give ratio,
    * so that the ratio is evenly preserved for each distinct value of the given labelColumn.
    *
    * @param ratio value between 0 and 1 representing the ratio of the first split
    * @param labelColumn
    * @param data
    * @return
    */
  def splitEvenlyByLabel(ratio: Double, labelColumn: Int, data: RDD[Array[_]]):
  (RDD[Array[_]], RDD[Array[_]]) = {

    require(ratio > 0 && ratio < 1, "The given ratio should be in the (0, 1) interval (open interval).")

    val colsSize = data.first.length
    require(labelColumn >= 0 && labelColumn < colsSize, s"Label column must be in range 0 to $colsSize")

    val distinctLabels = data.map(row => row(labelColumn)).distinct.collect

    // TODO This is highly unoptimized :) so it should be optimized
    distinctLabels.map { label =>
      val splits = data.filter(row => row(labelColumn) == label).randomSplit(Array(ratio, 1-ratio))
      if(splits.size == 1)
        (splits(0), splits(0))
      else
        (splits(0), splits(1))
    }.reduce { (x1, x2) => (x1._1.union(x2._1), x1._2.union(x2._2)) }
  }


  def printJson[T <: scala.AnyRef](that: T)(implicit mf: Manifest[T]) = {
    import org.json4s._
    import org.json4s.jackson.Serialization._
    implicit val formats = DefaultFormats + FieldSerializer[T]()

    println(writePretty(that))

  }

}
