# Cuisine Prediction Training

## Usage notes

There are several scripts available for convenience to run this project, located in the `scripts` directory.

**`setenv.sh`**
- set the configuration for all the available scripts
- if this is not correct the planets will not align!

**`run-all.sh`**
- run the entire project, from data import, model generation and prediction generation

**`import-data.sh`**
- import the training json data, process it and save it along with the meta-data (labels and features indices)

**`build-models.sh`**
- based on the imported data, build the prediction models for LogisticRegression, NaiveBayes, DecisionTree and RandomForest.
- once the `import-data.sh` script was ran, this can be ran multiple times
- the results will be slightly different each time and the main reason is that the training data is split in a random fashion

**`build-predictions.sh`**
- this is the last step of the process and it generates the predictions using the models generated by `build-models.sh`
- once the `build-models.sh` script was ran, this script can be ran multiple times with different inputs

Some information about the currently generated models can be found [here](cuisines_data_analysis_notes.md).

## Running on Yarn cluster

### Preparation
- copy the fat jar to a location on a cluster machine (e.g. on the spark master node)
- put the train data and test data in hdfs

### Running
- ssh the machine where you are running `spark-submit`
- run almost the entire app with something like (it does not run the export to elastic seach)

```
spark-submit --class "com.lunatic.mlx.cuisines.MainMlLib"
--master yarn-cluster \
--num-executors 3 \
--driver-memory 1024m --executor-memory 1024m \
  /tmp/machine-learning-fat.jar \
  app.input.file.training=hdfs://192.168.100.10/user/spark/train.json \
  app.input.file.test=hdfs://192.168.100.10/user/spark/test.json \
  app.model.dir=hdfs://192.168.100.10/user/spark/working_data \
  app.output.file.predictions=hdfs://192.168.100.10/user/spark/predictions
```

- other options for class are: 
  - `com.lunatic.mlx.cuisines.ImportData`
  - `com.lunatic.mlx.cuisines.BuildModels`
  - `com.lunatic.mlx.cuisines.BuildPredictions`
  - `com.lunatic.mlx.cuisines.ExportToES` useful for exporting the predictions to ES

## Design notes

### Main entry points

| Class | Role |
| ----- | ---- |
| `MainMlLib`            | Run data import, build models and predictions        |
| `ImportData`           | Get data from json file into an `RDD[LabeledVector]` |
| `BuildModels`          | Train different models using the same training data  |
| `BuildPredictions`     | Predict cuisines for test recipes                    |

### Main interfaces

| Class | Role |
| ----- | ---- |
| `Model`            | Interface unification for all Spark MLLib model classes       |
| `Trainer`          | Interface unification for all Spark MLLib trainer classes     |
| `Transformer`      | Interface unification for all Spark MLLib transformer classes |
| `SparkRunner`      | Ridiculous interface for running the main entry points in a sequence |

## TODOs

- [x] Add shell scripts for convenience
- [x] Save prediction results as json (easy to sent to ElasticSearch)
- [ ] Fix the ChiSqSelectorTransformer
- [x] Test in cluster environment
- [ ] Research spark unit testing
- [ ] Add unit tests
- [ ] Add trainer configuration and pass it from the running scripts as arguments
- [ ] Add logging
- [ ] Trainer train should take a splitter function instead of a double
- [ ] Add DAOs (and remove the DaoUtils monstrosity)
- [ ] FlowData -> Monad
- [ ] MulticlassMetrix beautification
- [ ] More comments (hopefully meaningful)
- [ ] Configuration...
- [ ] Style improvements (Option, Try...)
- [ ] Use functional composition more
- [ ] ...
- [ ] Last but not least, remove the smirks

[Home](../README.md)
