package com.lunatic.mlx.cuisines

/**
  * Some basic paths configuration
  */
case class Configuration(args: Array[String]) {

  // TODO: Refactor and improve

  val argsMap = args.map(_.split("=")).map(x => (x(0), x(1))).toMap

  val inputTrainingData = argsMap.getOrElse("app.input.file.training", "data/cuisines/train.json")
  
  val inputTestingData = argsMap.getOrElse("app.input.file.test", "data/cuisines/test.json")

  val outputPredictionsPath = argsMap.getOrElse("app.output.file.predictions", "/tmp/predictions")
  
  private val modelRootPath = argsMap.getOrElse("app.model.dir", "working_model")

  val dataPath = s"$modelRootPath/flow_data"

  val recipesPath = s"$modelRootPath/recipes"

  private val trainingDataRoot = s"$modelRootPath/training/"

  val naiveBayesPath = trainingDataRoot + "naive_bayes.model"

  val logisticRegPath = trainingDataRoot + "logistic_regression.model"

  val decisionTreePath = trainingDataRoot + "decision_tree.model"

  val randomForestPath = trainingDataRoot + "random_forest.model"


  val es_index_auto_create = argsMap.getOrElse("es.index.auto.create", "true")

  val es_nodes = argsMap.getOrElse("es.nodes","localhost")

  val es_port = argsMap.getOrElse("es.port","9200")

}


