package com.lunatic.mlx.kddcup99.mllib.transformers

import org.apache.spark.rdd.RDD

import scala.reflect.ClassTag

/**
  * Remove the columns in the given list
  */
case class ColumnRemover[T: ClassTag](removableColumns: Option[List[Int]] = None) extends Transformer[Array[T], Array[T]] {

//class ColumnRemover[T: ClassTag] extends Transformer[Array[T], Array[T], ColumnRemover[T]] {

  override def transform(input: RDD[Array[T]]): RDD[Array[T]] =
    removableColumns.map(rc =>
      input.map { v => v.zipWithIndex.filterNot(x => rc.contains(x._2)).map(_._1) }
    ).getOrElse(input)

  override def transform(input: Array[T]): Array[T] = {
    removableColumns.map(rc =>
      input.zipWithIndex.filterNot(x => rc.contains(x._2)).map(_._1)
    ).getOrElse(input)
  }

//  type TransParams <: ColumnRemovedParams
//
//  override def transform(input: RDD[Array[T]], params: Option[TransParams] = None): RDD[Array[T]] =
//    params.map(rc =>
//    input.map { v => v.zipWithIndex.filterNot(x => rc.removableColumns.contains(x._2)).map(_._1) }
//  ).getOrElse(input)
//
//  override def transform(input: Array[T], params: Option[TransParams] = None): Array[T] = {
//    params.map(rc =>
//      input.zipWithIndex.filterNot(x => rc.removableColumns.contains(x._2)).map(_._1)
//    ).getOrElse(input)
//  }

}
//case class ColumnRemovedParams[T: ClassTag](removableColumns: List[Int]) extends Params[ColumnRemover[T]]
