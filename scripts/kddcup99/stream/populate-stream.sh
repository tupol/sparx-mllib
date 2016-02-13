#!/usr/bin/env bash

# What do I do?
###############

# Put some files for prediction in the test input stream directory
# The main use case is development and test.
#
# NOT FOR PRODUCTION USE!!!

# CONFIGURATION SECTION
#######################

# Set the global configuration

rundir="`dirname "$0"`"
rundir="`cd "$rundir"; pwd`"
. "$rundir"/setenv.sh

TEMP_DIR="/tmp/streaming_kmeans_temp"
rm -rf $TEMP_DIR
mkdir $TEMP_DIR

hadoop fs -copyToLocal "$APP_INPUT_FILE_TEST" "$TEMP_DIR"

IN_FILE_NAME=`ls $TEMP_DIR | xargs -n1 basename`
SPLITTABLE_FILE="$TEMP_DIR/$IN_FILE_NAME"

echo $SPLITTABLE_FILE

cd $TEMP_DIR

# Split in chunks of X lines
split -l 10000 "$SPLITTABLE_FILE"

rm -f "$SPLITTABLE_FILE"

# RUN SECTION
#############

hdfs dfs -rm -r /playground/demo/in
hdfs dfs -mkdir /playground/demo/in
hdfs dfs -mkdir /playground/demo/in/test

for file in ./*
do
 hdfs dfs -copyFromLocal $file "$APP_INPUT_STREAMING_TESTING"
 echo "Moved $file to $APP_INPUT_STREAMING_TESTING"
 sleep 23
done

