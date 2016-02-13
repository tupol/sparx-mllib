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
  --class "nl.ing.mlx.kddcup99.modgen.Main" \
  --name $RUNNING_PREFIX_"DemoModGen-FullProcess" \
  --master $SPARK_MASTER_URL \
  --num-executors $YARN_EXECUTORS \
  --executor-cores $YARN_CORES \
  --conf spark.task.maxFailures=20 \
  --conf spark.driver.memory=$SPARK_DRIVER_MEMORY \
  --conf spark.executor.memory=$SPARK_EXECUTOR_MEMORY \
  $JAR_FILE \
  app.input.file.training="$APP_INPUT_FILE_TRAINING" \
  app.input.file.test="$APP_INPUT_FILE_TEST" \
  app.output.path="$APP_OUTPUT_PATH" \
  app.wip.path="$APP_WORK_IN_PROGRESS" \
  app.kmeans.clusterRange="$APP_KMEANS_CLUSTER_RANGE" \
  app.kmeans.iterationRange="$APP_KMEANS_ITERATION_RANGE" \
  app.kmeans.epsilonRange="$APP_KMEANS_EPSILON_RANGE"
