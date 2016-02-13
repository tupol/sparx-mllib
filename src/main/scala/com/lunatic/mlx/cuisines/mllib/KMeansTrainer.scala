package com.lunatic.mlx.cuisines.mllib

import com.lunatic.mlx.cuisines.Configuration
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.{SparkConf, SparkContext}



object KMeansTrainer {


  def main(args: Array[String]) = {

    val conf = new SparkConf(true).setAppName(this.getClass.getSimpleName).
      setMaster("local[*]")

    implicit val sc = new SparkContext(conf)
    implicit val configuration = Configuration(args)

    val flowData = FlowData.load(configuration.dataPath)

    val parsedData = flowData.data.map(lp => lp.features)

    // Cluster the data into two classes using KMeans
    val numClusters = 20
    val numIterations = 1000
    val runs = 5
    val clusters = KMeans.train(parsedData, numClusters, numIterations, runs)



    // Evaluate clustering by computing Within Set Sum of Squared Errors
    val WSSSE = clusters.computeCost(parsedData)
    println("Within Set Sum of Squared Errors = " + WSSSE)

    val predictionsClusters =  flowData.data.map(lp => (lp.label.toInt, clusters.predict(lp.features)))

    predictionsClusters.groupBy(_._1).collect.foreach{case (label, pred) =>

      val clustersWeights = pred.map(x => x._2)
        .groupBy(x => x).map(x => (x._1, x._2.size))
        .toSeq.sortBy(x => x._2).reverse.take(5)
        .map(x => f"${x._1}%2d : ${x._2}%4d")
        .mkString(" | ")

      println(f"| ${flowData.indexToLabel(label)}%-20s | ${label}%02d | ${clustersWeights} | ")
    }

  }
}


