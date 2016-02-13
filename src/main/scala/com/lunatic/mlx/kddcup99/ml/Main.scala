package com.lunatic.mlx.kddcup99.ml

import com.lunatic.mlx.kddcup99.mllib.metadata.KddRecord
import KddRecord._
import com.lunatic.mlx.kddcup99.ml.pipelines.KddPipeline
import com.lunatic.mlx.kddcup99.mllib.Configuration
import com.lunatic.mlx.kddcup99.mllib.modgen.SparkRunnable
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext

/**
 *
 */
object Main extends SparkRunnable {

  def run(implicit sc: SparkContext, appConf: Configuration) = {

    val sqc = new SQLContext(sc)

    val labeledData = sc.textFile(appConf.inputTrainingData).map(_.split(",")).cache

    val splitRatio = 0.7

    val labelColumn = 41

//    val (trainData, testData) = SplitTrainingData.splitEvenlyByLabel(splitRatio, labelColumn, labeledData)
//
//    val kdds = trainData.map(toKddRecord)

    val kdds = labeledData.map(toKddRecord)

    val data = sqc.createDataFrame(kdds, schema)

    val pipeline = KddPipeline()


//    println("---------------------------------")
//    println("Pipeline Description")
//    println(pipeline.toString())
//    println(pipeline.explainParams())
//    println(pipeline.extractParamMap())
//    println("---------------------------------")
//    println("Pipeline Stages")
//    pipeline.getStages.foreach{ stage =>
//      println(s"---")
//      println(s"* $stage")
//      println(stage.explainParams())
//      stage.extractParamMap().toSeq.foreach{ param =>
//        println(f"  - ${param.param.name}%-20s - ${param.value}")
//      }
//    }



    sys.exit

    // Split the data into training and test sets (5% held out for testing)
    val Array(trainingData, testData) = data.randomSplit(Array(0.2, 0.8))

    val model = pipeline.fit(data)

    val predictions = model.transform(trainingData)

    println("---------------------------------")
    println("Predictions Schema")
    predictions.printSchema()

    println("---------------------------------")
    println("Predictions Sample")

    predictions.take(10).foreach(println)

    println("---------------------------------")
    println("Pipeline Model Description")
    println(model.toString())
    println(model.explainParams())
    println(model.extractParamMap())
    model.extractParamMap().toSeq.foreach{ param =>
      println(f"  - ${param.param.name}%-20s - ${param.value}")
    }
    model.stages.foreach{ stage =>
      println(s"---")
      println(s"* $stage")
      println(stage.explainParams())
      stage.extractParamMap().toSeq.foreach{ param =>
        println(f"  - ${param.param.name}%-20s - ${param.value}")
      }
    }

  }


}




