package com.lunatic.mlx.kddcup99.mllib.streaming

import com.lunatic.mlx.kddcup99._
import com.lunatic.mlx.kddcup99.mllib.Configuration
import com.lunatic.mlx.kddcup99.mllib.metadata.KMeansXMD
import org.apache.spark.mllib.clustering.StreamingKMeans

import scala.util.{Failure, Success}

/**
  * KMeans Extended model description loader
  */
object KxmdLoader {

  /**
    * Load a list of stationary models from the application parameters
 *
    * @return
    */
  def loadModels(implicit appConf: Configuration): List[KMeansXMD] = {
    appConf.argsMap.
      get("app.prediction.models").
      map {
        _.split(",").map { file =>
          loadObjectFromFile[KMeansXMD](file.trim) match
          {
            case Success(m) => println(s"Successfully loaded model from $file"); Some(m)
            case Failure(x) => sys.error(s"Failed to load model from $file.\n${x.getLocalizedMessage}"); None
          }
        }.flatten.toList
      }.getOrElse(List[KMeansXMD]())

  }

  /**
    * Implicit conversion from KMeansXMD to StationaryModel
 *
    * @param kxmd
    * @return
    */
  implicit def kxmdToStreaming(kxmd: KMeansXMD): StreamingKMeans = {
    val clusterCenters = kxmd.model.clusterCenters
    // for now we default to 1, but soon they will be available in KXMD
    val centerWeights = clusterCenters.map(_ => 1.0)
    val k = kxmd.model.k
    val decayFactor = 1.0 // Keep all

    new StreamingKMeans()
      .setK(k)
      .setDecayFactor(decayFactor)
      .setInitialCenters(clusterCenters, centerWeights)
  }

}
