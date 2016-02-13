package com.lunatic.mlx.kddcup99.mllib.streaming

import com.lunatic.mlx.kddcup99.mllib.metadata._
import com.lunatic.mlx.kddcup99.mllib.metadata.KddRecord._
import com.lunatic.mlx.kddcup99.mllib.streaming.KxmdLoader._
import com.lunatic.mlx.kddcup99.mllib._
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.StreamingContext
import org.elasticsearch.spark.sql._

import scala.util.Try

/**
  * Consume a stream of strings and transform it into a file of predictions
  *
  * TODOs:
  * [ ] figure out how to load and hot-swap the KMeans models (e.g. over time we might want to swap the KMeans
  * while the stream is still on;
  * [ ] figure out how to persist the predictions and what should go in there (e.g. predicted cluster,
  * distance to centroid, original input data, KMeans model version/id/timestamp);
  * [ ] figure out how to train, monitor training behavior and save the models trained
  */
object StreamPredictor extends SparkRunnable {

  def run(implicit ssc: StreamingContext, appConf: Configuration) = {

    implicit val sc = ssc.sparkContext
    implicit val sqc = new SQLContext(sc)

    // TODO implement a watchdog to load new models from a directory; the StreamingListener might be a good starting point
    // Get the models that we want to use for predictions
    val modelsX = loadModels

    if(modelsX.isEmpty) {
      //TODO Figure out if sys.exit is ok for a spark app, maybe not
      sys.error("No valid models specified.")
      sys.exit(-1)
    }

    val rawTestData = ssc.textFileStream(appConf.streamingInputTestingDir).map(line => line.split(","))

    val testDataLabeled = rawTestData.map(toKddRecord)

    // Initialize the streaming kMeans predictors
    val predictionsByModel = modelsX.map { mx =>
        val predictions = testDataLabeled.map { kddr =>
        val vector = mx.preProcessor.transform(kddr.data.map(_.toString))
        val model = mx.latestModel()
        val k = model.predict(vector)
        val distance = Vectors.sqdist(vector, model.clusterCenters(k))
        val prediction = Prediction(k, distance, mx.id, kddr.data, kddr.label.getOrElse("???"))

        val anomalyByStrategy = mx.anomalyThresholds.map { case (name, thresholds) =>
          val threshold = thresholds(k)
          (name, distance > threshold)
        }
        AnalysedPrediction(anomalyByStrategy, prediction)
      }
      (mx, predictions)
    }


    // Output something on the console, in a file and in ES for each output stream of each StreamingKMeans
    predictionsByModel.foreach { case(kMeansXMD, output) =>

      // Produce some printable output and write it to a file
      output.map(_.toString).saveAsTextFiles(appConf.outputPath + "/streaming/predictions/pred")

      // Export to ES
      val schema = AnalysedPrediction.schema(kMeansXMD.anomalyThresholds.map(_._1))
      output.foreachRDD{ rdd =>
        Try{
          val dataDF = sqc.createDataFrame(rdd, schema)
          dataDF.saveToEs(appConf.esIndexRoot + "/predictions_stream")
        }
      }
    }

    ssc.addStreamingListener(StreamingKMeansListener(modelsX))
    ssc.start
    ssc.awaitTermination

  }

}
