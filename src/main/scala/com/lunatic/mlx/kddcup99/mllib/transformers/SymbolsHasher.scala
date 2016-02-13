package com.lunatic.mlx.kddcup99.mllib.transformers

import org.apache.spark.rdd.RDD

import scala.util.hashing.MurmurHash3

/**
  * Given a list of symbolic columns (usually non-numeric) map it into a list of doubles and save the map.
  *
  * Ideally this should be an isomorphic transformer (@see IsoTransformer).
  */
case class SymbolsHasher(symboliColumns: Option[List[Int]], dictionary: Option[Seq[Map[String, Int]]])
  extends EducatedTransformer[Array[String], Array[Double]] {

  private val seed = 1259

  def this(symboliColumns: Option[List[Int]]) = this(symboliColumns, None)

  /**
    * Hash all the symbolic columns and produce a dictionary for retro-translation
    *
    * @param input by default the input should consist of unlabeled data
    * @return
    */
  override def learn(input: RDD[Array[String]]): EducatedTransformer[Array[String], Array[Double]] = {

    val zero = (1 to input.first.size).map(_ => Set[String]()).toArray

    val maps: Option[Seq[Map[String, Int]]] = symboliColumns.map { sc =>
      input.aggregate(zero)(
        { (acc, a1) => acc.zip(a1).zipWithIndex.
          map { case ((s, a), idx) => if (sc.contains(idx)) s + a else s }
        }, { (s1, s2) => s1.zip(s2).map { s => s._1 ++ s._2 } }
      ).
        map(s => s.map(x => (x, hasher(x))).toMap).
        toSeq
    }
    SymbolsHasher(symboliColumns, maps)
  }

  private def hasher(string: String): Int =
    MurmurHash3.stringHash(string, seed)


  /**
    * Hash the symbols into integers then convert everything to doubles.
    *
    * !! ATTENTION !! This can fail, and so it should until further refactoring.
    * What do we do if in the test data we have undefined labels? Ideally we should educate a new SymbolHasher
    * by adding a new entry then and persist it. Continuous learning, or so they say...
    *
    * @param input
    * @return
    */
  override def transform(input: RDD[Array[String]]): RDD[Array[Double]] = {
    // I was debating with myself here weather or not to map the dictionary or require it... and I still am
    require(dictionary.isDefined)
    input.map(transform)
  }

  /**
    * Hash the symbols into integers then convert everything to doubles.
    *
    * !! ATTENTION !! This can fail, and so it should until further refactoring.
    * What do we do if in the test data we have undefined labels? Ideally we should educate a new SymbolHasher
    * by adding a new entry then and persist it. Continuous learning, or so they say...
    *
    * @param input
    * @return
    */
  override def transform(input: Array[String]): Array[Double] = {
    require(dictionary.isDefined)
    // TODO MANAGE NEW LABELS (UNKNOWN BY THE DICTIONARY)
    input.zip(dictionary.get).map{ case(currentValue, dict) =>
      if(dict.isEmpty)
        currentValue.toDouble
        else
        dict.get(currentValue).getOrElse(hasher(currentValue)).toDouble
    }
  }
}
