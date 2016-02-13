package com.lunatic.mlx.kddcup99.mllib.transformers

import org.apache.spark.rdd.RDD

/**
  * Transform a A into an B or an RDD[A] into an RDD[B]
  */
trait Transformer[A, B] {
//trait Transformer[A, B, R <: Transformer] {

  def transform(input: RDD[A]): RDD[B]

  def transform(input: A): B

//  type TransParams <: Params[R]
//
//  def transform(input: RDD[A], params: Option[TransParams]): RDD[B]
//
//  def transform(input: A, params: Option[TransParams]): B

}

//trait Params[T <: Transformer]

