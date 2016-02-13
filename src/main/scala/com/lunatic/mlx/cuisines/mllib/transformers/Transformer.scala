package com.lunatic.mlx.cuisines.mllib.transformers

import com.lunatic.mlx.cuisines.mllib.FlowData

/**
 * Interface for transforming flow data (e.g. filtering, sorting,
 * applying smart algorithms on it...)
 */
trait Transformer[T] {

  def transform(input: T): FlowData

}
