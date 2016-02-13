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
export YARN_EXECUTORS=4
export YARN_CORES=3
export SPARK_DRIVER_MEMORY=6g
export SPARK_EXECUTOR_MEMORY=4g


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
export JAR_FILE="/tmp/DemoModGen-fat.jar"
### Files and folders
export ROOT_DIR="hdfs:///playground/demo"
# Training data input json file
# Entire data set
#export APP_INPUT_FILE_TRAINING="hdfs:///playground/kdd/kddcup.data"
# 10% of the data set
export APP_INPUT_FILE_TRAINING="hdfs:///playground/kdd/kddcup.data_10_percent"
# Testing data inout json file
export APP_INPUT_FILE_TEST="hdfs:///playground/kdd/kddcup.testdata.unlabeled"
# The output containing all the predictions made on the APP_INPUT_FILE_TEST file
export APP_OUTPUT_PATH="$ROOT_DIR/out"
# Where to store partial data (like training data and meta-data)
export APP_WORK_IN_PROGRESS="$ROOT_DIR/wip"
# In streaming mode read new training entries from files in this directory
export APP_INPUT_STREAMING_TRAINING="$ROOT_DIR/in/train"
# In streaming mode read new testing entries from files in this directory
export APP_INPUT_STREAMING_TESTING="$ROOT_DIR/in/test"
#
# Arguments for KMeans Training Template
# User inputs a range for which to train several KMeans models.
# In order to define a KMeansTrainingTemplate we require 3 input parameters for (cluster, iterations, epsilon).
# As these values are ranges, we ask the user to supply a single String indicating the (start, stop, step) for each input.
# i.e. assuming we want to train a model that would test clusters from 145 to 155 with an interval of 5
# we pass 145,155,5 as input for this argument as a String (without spaces) separating the values for (start, stop, step) by commas.
#
# no. of cluster ranges to explore (start with n clusters, stop after running this cluster, step by n)
export APP_KMEANS_CLUSTER_RANGE="140,150,10"
# no. of iteration ranges to explore (start with n, stop after running this iteration, step by n)
export APP_KMEANS_ITERATION_RANGE="50,50,10"
# KMeans default value for epsilon = 1E-5 or 10 raised to (-5)
# user inputs the power (converted to negative) by which to raise 10
# no. of epsilon ranges to explore (start with n, stop after running this epsilon, step by n)
export APP_KMEANS_EPSILON_RANGE="10,10,1"
