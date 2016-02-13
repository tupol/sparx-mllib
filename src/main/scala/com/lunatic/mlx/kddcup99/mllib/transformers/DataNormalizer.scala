package com.lunatic.mlx.kddcup99.mllib.transformers

import com.lunatic.mlx.kddcup99.mllib.metadata.MVSS
import org.apache.spark.annotation.Experimental
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.rdd.RDD

/**
  * Normalize the data given a normalization algorithm.
  *
  * Ideally this should be an isomorphic transformer (@see IsoTransformer).
  */
case class DataNormalizer(normalizer: String, mvss: Option[MVSS])
  extends EducatedTransformer[Vector, Vector] {

  import DataNormalizer._

  def this(normalizer: String) = this(normalizer, None)

  override def learn(input: RDD[Vector]): EducatedTransformer[Vector, Vector] = {
    DataNormalizer(normalizer, Some(MVSS(Statistics.colStats(input))))
  }

  override def transform(input: RDD[Vector]): RDD[Vector] = {
    require(mvss.isDefined)
    normalizer match {
      case L0Norm => input
      case L1Norm => input.map(v => normalizeL1(v, mvss.get))
      case L2NormV1 => input.map(v => normalizeL2(v, mvss.get))
    }
  }


  override def transform(input: Vector): Vector = {
    require(mvss.isDefined)
    normalizer match {
      case L0Norm => input
      case L1Norm => normalizeL1(input, mvss.get)
      case L2NormV1 => normalizeL2(input, mvss.get)
    }
  }


  /**
    * L2 Normalization by column
    *
    * @param data
    * @return
    */
  private def normalizeL2(data: Vector, mvss: MVSS): Vector = {

    val mean = mvss.mean.toArray
    val stdev = mvss.stdev.toArray

    Vectors.dense(
      data.toArray
        .zip(mean).map { case (x, m) => m - x }
        .zip(stdev).map { case (x, s) => x / s }
    )
  }


  /**
    * L1 Normalization by column
    *
    * @param data
    * @return
    */
  private def normalizeL1(data: Vector, mvss: MVSS): Vector = {

    val width = mvss.width.toArray
    val mid = mvss.mid.toArray

      Vectors.dense(
        data.toArray
          .zip(mid).map { case (x, m) => x - m }
          .zip(width).map { case (x, w) => x / w }
      )
  }

  /**
    * Expanded column L2 Normalization unoptimized
    *
    * @param data
    * @return
    */
  @Experimental
  private def rawNormalizeColumnsL2(data: RDD[Vector]) = {
    import math._
    val cols = data.first.size
    val size = data.count

    val da = data.map(_.toArray)
    val da2 = data.map(_.toArray).map(arr => arr.map(x => x * x))

    val sums = da.reduce { (v1, v2) =>
      val zv = v1.zip(v2)
      zv.map { case (a, b) => a + b }
    }
    val sum2 = da2.reduce { (v1, v2) =>
      val zv = v1.zip(v2)
      zv.map { case (a, b) => a + b }
    }
    val means = sums.map(_ / size)

    val variance = da.map(v => v.zip(means).map { case (x, m) => (x - m) * (x - m) }).reduce { (v1, v2) =>
      val zv = v1.zip(v2)
      zv.map { case (a, b) => a + b }
    }.map(_ / size)

    val stdev = variance.map(sqrt(_))

    da.map(v => v.zip(means).map { case (x, m) => x - m }.zip(stdev).map { case (x, s) => x / s }).map(Vectors.dense(_))

  }
}

object DataNormalizer {

  // No normalization; return original data
  val L0Norm = "L0Norm"
  // L1Norm applied by column, using MultivariateStatisticalSummary
  // This is basically just a scaler (
  val L1Norm = "L1Norm"
  // L2Norm applied by column, using MultivariateStatisticalSummary
  val L2NormV1 = "L2NormV1"
  // L2Norm own algorithm applied by column
  //  val L2NormV2 = "L2NormV2"
}
