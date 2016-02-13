package com.lunatic.mlx.kddcup99.ml.pipelines

import org.apache.spark.ml.Pipeline

/**
  *
  */
trait PipelineFactory[C <: PipelineConfiguration] {

  def apply(conf: C): Pipeline
  def apply(): Pipeline

}

trait PipelineConfiguration

