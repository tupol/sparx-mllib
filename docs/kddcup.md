# KDD Cup Anomaly Detection

## Usage notes

Some information about the currently generated models can be found for each experiments session as follows:
- [Session 01](kddcup_data_analysis_notes_s1.md); tried various normalization algorithms searching for a good K choice
- [Session 02](kddcup_data_analysis_notes_s2.md); tried various metrics searching for a good K choice

## Running on Yarn cluster

### Preparation
- copy the fat jar to a location on a cluster machine (e.g. on the spark master node)
- put the train data and test data in hdfs

### Running
- ssh the machine where you are running `spark-submit`
- run almost the entire app with something like (it does not run the export to elastic seach)

```
spark-submit --class "com.lunatic.mlx.kddcup99.stationary.Main"
--master yarn-cluster \
--num-executors 4 \
--spark.driver.cores 3 \
--driver-memory 4g --executor-memory 3g \
  /tmp/machine-learning-fat.jar \
  app.input.file.training=hdfs:///playground/kdd/kddcup.data \
  app.input.file.test=hdfs:///playground/kdd/kddcup.testdata.unlabeled \
  app.wip.path=hdfs:///playground/OT/wip \
  app.output.file.predictions=hdfs:///playground/OT/predictions
```

- other options for class are: 
  - `com.lunatic.mlx.kddcup99.stationary.AnalyseInputData`
  - `com.lunatic.mlx.kddcup99.stationary.EducateTransformers`
  - `com.lunatic.mlx.kddcup99.stationary.SplitTrainingData`
  - `com.lunatic.mlx.kddcup99.stationary.PreProcessData`
  - `com.lunatic.mlx.kddcup99.stationary.TrainKMeans`
  - `com.lunatic.mlx.kddcup99.stationary.PredictKMeans`

## Design notes

### Main entry points

| Class                   | Role            |
| ----------------------- | --------------- |
| `ImportData`            | Run data import, normalize...                        |
| `TrainKMeans`           | Train KMeans using hte normalized data               |
| `PredictKMeans`         | Not exactly what it seems, but might be some day...  |
| `ExportToESRaw    `     | Export the raw, un-normalized data to ES             |

## TODOs

- [ ] Add shell scripts for convenience
- [ ] Research spark unit testing
- [ ] Add unit tests
- [ ] Add logging
- [ ] More comments (hopefully meaningful)
- [ ] Configuration...
- [ ] Style improvements (Option, Try...)
- [ ] Use functional composition more
- [ ] Improve the readme(s)
- [ ] ...
- [ ] Last but not least, remove the smirks

[Home](../README.md)
