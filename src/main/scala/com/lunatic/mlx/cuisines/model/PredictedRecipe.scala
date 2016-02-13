package com.lunatic.mlx.cuisines.model

import com.lunatic.mlx.cuisines.mllib.ClassMetrics

/**
 *
 */

case class PredictedRecipe(id: Int, ingredients: Seq[String], predictions: Seq[PredictionData])

case class PredictionData(model: String, prediction: String, metrics: ClassMetrics)
