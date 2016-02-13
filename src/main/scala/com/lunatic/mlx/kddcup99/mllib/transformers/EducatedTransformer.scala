package com.lunatic.mlx.kddcup99.mllib.transformers

import org.apache.spark.rdd.RDD

/**
  * Unlike a normal transformer this one needs to learn something about the data to be transformed
  */
trait EducatedTransformer[T, R] extends Transformer[T, R] {

  /**
    * Learn form the input and produce a transformation model that can be used in the future
 *
    * @param input by default the input should consist of unlabeled data
    * @return
    */
  def learn(input: RDD[T]): EducatedTransformer[T, R]

}
