#!/usr/bin/env bash

# What do I do?
###############

# Run the entire project, from data import, model generation and prediction generation.

# CONFIGURATION SECTION
#######################

# Set the global configuration

rundir="`dirname "$0"`"
rundir="`cd "$rundir"; pwd`"
. "$rundir"/setenv.sh


# RUN SECTION
#############

spark-submit \
  --class "nl.ing.mlx.kddcup99.streaming.StreamPredictor" \
  --name $RUNNING_PREFIX_"DemoPrediction" \
  --master $SPARK_MASTER_URL \
  --num-executors $YARN_EXECUTORS \
  --executor-cores $YARN_CORES \
  --conf spark.task.maxFailures=20 \
  --conf spark.driver.memory=$SPARK_DRIVER_MEMORY \
  --conf spark.executor.memory=$SPARK_EXECUTOR_MEMORY \
  $JAR_FILE \
  app.input.file.test="$APP_INPUT_FILE_TEST" \
  app.output.path="$APP_OUTPUT_PATH" \
  app.input.streaming.training="$APP_INPUT_STREAMING_TRAINING" \
  app.input.streaming.testing="$APP_INPUT_STREAMING_TESTING" \
  app.streaming.checkpoint="$APP_STREAMING_CHECKPOINT" \
  app.streaming.batch.seconds="$APP_STREAMING_BATCH_SECONDS" \
  app.prediction.models="$APP_PREDICTION_MODELS"
