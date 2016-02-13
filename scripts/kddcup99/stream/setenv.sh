#!/usr/bin/env bash

# For development testing reasons, we need to reassembly when we change the code
# sbt assembly

# PARAMETERS SECTION
####################

# In case multiple people are using the lab vms, each should have a different app prefix
# Personal initials can be a good starting point
export RUNNING_PREFIX="XY"

### Spark-Submit ###
####################
export SPARK_MASTER_URL=yarn-cluster
export YARN_EXECUTORS=2
export YARN_CORES=2
export SPARK_DRIVER_MEMORY=4g
export SPARK_EXECUTOR_MEMORY=3g


### Elastic Search ###
######################
# Elastic search auto create
export ES_INDEX_AUTO_CREATE=true
# Elastic Search host
export ES_NODES=localhost
# Elastic Search port
export ES_PORT=9200



### Application Parameters ###
##############################
# The location of the fat-jar for the application (run `sbt assembly` to create it)
export JAR_FILE="/tmp/DemoPrediction-fat.jar"
### Files and folders
export ROOT_DIR="hdfs:///playground/demo"
# Testing data inout json file
export APP_INPUT_FILE_TEST="hdfs:///playground/kdd/kddcup.testdata.unlabeled"
# The output containing all the predictions made on the APP_INPUT_FILE_TEST file
export APP_OUTPUT_PATH="$ROOT_DIR/out"
# In streaming mode read new training entries from files in this directory
export APP_INPUT_STREAMING_TRAINING="$ROOT_DIR/in/train"
# In streaming mode read new testing entries from files in this directory
export APP_INPUT_STREAMING_TESTING="$ROOT_DIR/in/test"
# Just in case one wants to use persisted streams, this needs to be set, and not to /tmp
export APP_STREAMING_CHECKPOINT="/tmp/"
# Batch interval in seconds
export APP_STREAMING_BATCH_SECONDS="20"
# Comma separated values of fully qualified paths/urls for the prediction models
# The prediction models in this case are KMeansXMD objects, containing all the information about the model,
# including the pre-processing steps, model id, model creation time, and possible in the future, who created it
export APP_PREDICTION_MODELS="$APP_OUTPUT_PATH/kmeans_1E-10_140_0050_01_L2NormV1.model.xmd.bin,$APP_OUTPUT_PATH/kmeans_1E-10_150_0050_01_L2NormV1.model.xmd.bin"
