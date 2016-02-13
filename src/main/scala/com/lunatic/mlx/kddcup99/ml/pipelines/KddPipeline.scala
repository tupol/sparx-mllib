package com.lunatic.mlx.kddcup99.ml.pipelines

import java.util.Properties

import com.lunatic.mlx.kddcup99.mllib.metadata.KddRecord
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.ml.feature.{MinMaxScaler, StringIndexer, VectorAssembler}


/**
  *
  */
object KddPipeline extends PipelineFactory[KddPipelineConfiguration] {

  /**
    * Build a KMeans pipeline given the configuration
    *
    * @param conf
    * @return
    */
  def apply(conf: KddPipelineConfiguration): Pipeline = {
    val continuousCols = KddRecord.columnsInfo.filter(_._2).map(_._1)
    val discreteCols = KddRecord.columnsInfo.filterNot(_._2).map(_._1)

    val stringIndexers = discreteCols.map { colName =>
      new StringIndexer()
        .setInputCol(colName)
        .setOutputCol(colName + "_indexed")
    }

    val toFeaturesVector = new VectorAssembler().
      setInputCols((discreteCols.map(_ + "_indexed") ++ continuousCols).toArray).
      setOutputCol("features")
    //  The following might be required in case we want to use StandardScaler with standardDeviation
    //    val toDenseVector = new SparseToDense().setInputCol("features").setToDense

    val scaleAndCenter = new MinMaxScaler().
      //      setWithMean(true).setWithStd(true).
      setInputCol("features").
      setOutputCol("scaledFeatures")

    // Trains a k-means model
    val kmeans = new KMeans()
      .setK(conf.k)
      .setMaxIter(conf.iterations)
      .setFeaturesCol("scaledFeatures")
      .setPredictionCol("prediction")

    // Build the pipeline
    val pipeline = new Pipeline()
      .setStages(stringIndexers.toArray
        :+ toFeaturesVector
        //  :+ toDenseVector
        :+ scaleAndCenter
        :+ kmeans
      )

    pipeline.validateParams()

    pipeline
  }

  /**
    * Build a KMeans pipeline using the default configuration
    *
    * @return
    */
  override def apply(): Pipeline = apply(KddPipelineConfiguration(Map()))

}


trait KddPipelineConfiguration extends PipelineConfiguration {

  def k: Int

  def iterations: Int
}

object KddPipelineConfiguration {
  def apply(args: Map[String, String]) = {
    new KddPipelineConfiguration {
      val k = args.getOrElse("app.kmeans.k", "10").toInt
      val iterations = args.getOrElse("app.kmeans.iterations", "10").toInt
    }
  }

}

