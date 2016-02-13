#!/usr/bin/env bash

# What do I do?
###############

# Export data to ElasticSearch


# CONFIGURATION SECTION
#######################

# Set the global configuration

rundir="`dirname "$0"`"
rundir="`cd "$rundir"; pwd`"
. "$rundir"/setenv.sh


# RUN SECTION
#############

spark-submit \
  --class "com.lunatic.mlx.cuisines.ExportToES" \
  --name "CuisineDataImporter" \
  --master $SPARK_MASTER_URL \
  --num-executors $YARN_EXECUTORS \
  --conf spark.task.maxFailures=20 \
  --conf spark.driver.memory=$SPARK_DRIVER_MEMORY \
  --conf spark.executor.memory=$SPARK_EXECUTOR_MEMORY \
  --verbose \
  $JAR_FILE \
  app.input.file.training="$APP_INPUT_FILE_TRAINING" \
  app.input.file.test="$APP_INPUT_FILE_TEST" \
  app.output.file.predictions="$APP_OUTPUT_FILE_PREDICTIONS" \
  app.model.dir="$APP_MODEL_DIR" \
  es.index.auto.create=$ES_INDEX_AUTO_CREATE
  es.nodes="$ES_NODES"
  es.port=$ES_PORT
