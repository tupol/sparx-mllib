package com.lunatic.mlx.kddcup99.mllib.metadata

import java.util.Date

import com.lunatic.mlx.kddcup99.mllib.transformers.Transformer
import org.apache.spark.mllib.clustering.KMeansModel
import org.apache.spark.mllib.linalg.Vector

/**
  * Expanded model description; everything one needs to predict, analyse predictions (anomaly detection)
  * and model quality information. The mother description of the KMeans model.
  */
trait ModelDescription[M] {
  /**
    * The name or id of the model (it should be fairly unique)
    */
  val id: String
  /**
    * When was this created?
    */
  val createdAt: Date
  /**
    * The pre-processing object used to feed the model
    */
  val preProcessor: Transformer[Array[String], Vector]
  /**
    * The actual KMeans model
    */
  val model: M
}

/**
  * KMeans model expanded description
  *
  * @param id The name or id of the model (it should be fairly unique)
  * @param createdAt
  * @param preProcessor The pre-processing object used to feed the model
  * @param model  The actual KMeans model
  * @param anomalyThresholds A map of name reflecting the threshold generation strategy used and the sequence
  *                          of thresholds corresponding to each cluster
  */
case class KMeansXMD(
                      val id: String,
                      val createdAt: Date,
                      val parameters: KMeansParams,
                      val preProcessor: Transformer[Array[String], Vector],
                      val model: KMeansModel,
                      val modelQI: KMeansModelQI,
                      val anomalyThresholds: Seq[(String, Seq[Double])]
                  ) extends ModelDescription[KMeansModel] {

  /**
    * Convenience function returning a new KMeansXMD containing the additional anomaly thresholds
 *
    * @param name
    * @param thresholds
    * @return
    */
  def addAnomalyThreshold(name: String, thresholds: Seq[Double]): KMeansXMD = {
    require(thresholds.size == model.clusterCenters.size)
    // TODO Later on we should put this back in, but for now we can live without it.
    //    require(!anomalyThresholds.toMap.keySet.contains(name))
    new KMeansXMD(id, createdAt, parameters, preProcessor, model, modelQI, (name, thresholds) +: anomalyThresholds)
  }

}

object KMeansXMD {
  def apply( parameters: KMeansParams,
             preProcessor: Transformer[Array[String], Vector],
             model: KMeansModel, modelQI: KMeansModelQI): KMeansXMD =
    new KMeansXMD(parameters.experimentId, new Date(), parameters,
      preProcessor, model, modelQI, Seq[(String, Seq[Double])]())
}


