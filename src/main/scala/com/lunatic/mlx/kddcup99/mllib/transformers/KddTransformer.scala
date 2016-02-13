package com.lunatic.mlx.kddcup99.mllib.transformers

import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.rdd.RDD

import scala.reflect.ClassTag

/**
  * In the end, this holds all the transformers that need to be applied to the input data
  * and can be serialised nicely after education.
  */
case class KddTransformer(symboliColumns: Option[List[Int]], normAlgo: String = DataNormalizer.L2NormV1,
                          removableColumns: Option[List[Int]] = None,
                          colRemover: Option[ColumnRemover[Double]] = None,
                          hasher: Option[SymbolsHasher] = None,
                          vectorizer: Option[Vectorizer] = None,
                          customNormalizer: Option[DataNormalizer] = None,
                          l1Normalizer: Option[DataNormalizer] = None)
  extends EducatedTransformer[Array[String], Vector] {

  override def transform(input: RDD[Array[String]]): RDD[Vector] = {
    require(colRemover.isDefined)
    require(hasher.isDefined)
    require(vectorizer.isDefined)
    require(customNormalizer.isDefined)
    require(l1Normalizer.isDefined)

    // TODO make this work (smarter generic types???)
    //    val tx: List[Transformer[_, _]] =
    //      List[Option[Transformer[_, _]]](
    //        colRemover,
    //        hasher,
    //        vectorizer,
    //        customNormalizer,
    //        l1Normalizer).flatten
    //    tx.foldLeft(vd)((rdd, trans) => trans.transform(rdd))


    input.map(compoTrans)
  }

  override def transform(input: Array[String]): Vector = {
    require(colRemover.isDefined)
    require(hasher.isDefined)
    require(vectorizer.isDefined)
    require(customNormalizer.isDefined)
    require(l1Normalizer.isDefined)

    compoTrans(input)
  }

  private def compoTrans(input: Array[String]): Vector = {

    val vd = vectorizer.get.transform(
      colRemover.get.transform(
        hasher.get.transform(input)))

    // TODO make this work (smarter generic types??? higher-kind types???)
    //            val tx: List[Function1[Any, Any]] =
    //              List[Option[Function1[Any, Any]]](
    //                colRemover,
    //                hasher,
    //                vectorizer,
    //                customNormalizer,
    //                l1Normalizer).flatten
    //    Function.chain(tx)(input)

    l1Normalizer.get.transform(
      customNormalizer.get.transform(
        vectorizer.get.transform(
          colRemover.get.transform(
            hasher.get.transform(input)))))
  }

  /**
    * Learn form the input and produce a transformation model that can be used in the future
 *
    * @param input the input should consist of unlabeled data
    * @return
    */
  override def learn(input: RDD[Array[String]]): EducatedTransformer[Array[String], Vector] = {

    // In case no columns are defined as to be removed we run a check anyway, just to be sure
    // This is an expensive operation, so we avoid running it willy–nilly
    val rc = removableColumns.orElse(Some(columnsWithEqualValues(input).toList))
    val colRemover = ColumnRemover[Double](rc)

    val hasher = new SymbolsHasher(symboliColumns).learn(input).asInstanceOf[SymbolsHasher]

    val vectorizer = Vectorizer()

    val vectorData = vectorizer.transform(colRemover.transform(hasher.transform(input)))

    val customNormalizer = new DataNormalizer(normAlgo).learn(vectorData).asInstanceOf[DataNormalizer]

    val normalizedData = customNormalizer.transform(vectorData)

    // Scale data down to the same range
    val l1Normalizer = new DataNormalizer(DataNormalizer.L1Norm).learn(normalizedData).asInstanceOf[DataNormalizer]

    KddTransformer(symboliColumns, normAlgo, rc, Some(colRemover), Some(hasher), Some(vectorizer), Some(customNormalizer), Some(l1Normalizer))
  }

  private def columnsWithEqualValues[T: ClassTag](data: RDD[Array[T]]) = {
    (0 until data.first.size).map(col => if (data.map(v => v(col)).distinct.count == 1) col else -1).filter(_ > 0)
  }
}
