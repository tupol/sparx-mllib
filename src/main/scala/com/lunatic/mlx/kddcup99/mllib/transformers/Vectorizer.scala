package com.lunatic.mlx.kddcup99.mllib.transformers

import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.rdd.RDD

/**
  * Basic transformer rendering an array of doubles into a linalg.Vector
  */
case class Vectorizer() extends IsoTransformer[Array[Double], Vector] {

  override def transform(input: RDD[Array[Double]]): RDD[Vector] = input.map(transform)

  override def transform(input: Array[Double]): Vector = Vectors.dense(input)

  override def reverse(input: RDD[Vector]): RDD[Array[Double]] = input.map(_.toArray)

  override def reverse(input: Vector): Array[Double] = input.toArray

}
