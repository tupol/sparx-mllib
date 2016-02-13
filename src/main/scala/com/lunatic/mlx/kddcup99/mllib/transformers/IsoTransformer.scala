package com.lunatic.mlx.kddcup99.mllib.transformers

import org.apache.spark.rdd.RDD

/**
  * This is an isomorphic transformer trait.
  * What is that? Well it's something like this: reverse(transform(x)) = x.
  */
trait IsoTransformer[T, R] extends Transformer[T, R] {

  def reverse(input: RDD[R]): RDD[T]

  def reverse(input: R): T

}
