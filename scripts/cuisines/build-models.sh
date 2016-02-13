#!/usr/bin/env bash

# What do I do?
###############

# Build the prediction models, based on the imported data, for LogisticRegression,
# NaiveBayes, DecisionTree and RandomForest.
# Once the `import-data.sh` script was ran, this can be ran multiple times.
# The results will be slightly different each time and the main reason is that
# the training data is split in a random fashion.


# CONFIGURATION SECTION
#######################

# Set the global configuration

rundir="`dirname "$0"`"
rundir="`cd "$rundir"; pwd`"
. "$rundir"/setenv.sh


# RUN SECTION
#############

spark-submit \
  --class "com.lunatic.mlx.cuisines.BuildModels" \
  --name "CuisineDataModelBuilding" \
  --master $SPARK_MASTER_URL \
  --num-executors $YARN_EXECUTORS \
  --conf spark.driver.host=$SPARK_DRIVER_HOST \
  --conf spark.task.maxFailures=20 \
  --conf spark.driver.memory=$SPARK_DRIVER_MEMORY \
  --conf spark.executor.memory=$SPARK_EXECUTOR_MEMORY \
  --verbose \
  $JAR_FILE \
  app.input.file.training="$APP_INPUT_FILE_TRAINING" \
  app.input.file.test="$APP_INPUT_FILE_TEST" \
  app.output.file.predictions="$APP_OUTPUT_FILE_PREDICTIONS" \
  app.model.dir="$APP_MODEL_DIR"
