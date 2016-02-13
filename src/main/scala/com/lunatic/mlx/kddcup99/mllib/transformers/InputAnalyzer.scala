package com.lunatic.mlx.kddcup99.mllib.transformers

import org.apache.spark.rdd.RDD

import scala.reflect.ClassTag

/**
  * Run some analysis on the input and provide info and suggestions (like what columns might be discarded)
  */
case class InputAnalyzer[T: ClassTag](records: Option[Long] = None, colNames: Option[Map[Int, String]] = None, distinctCountByColumn: Option[Map[Int, Map[T, Long]]] = None) extends EducatedTransformer[Array[T], Array[T]] {

  override def learn(input: RDD[Array[T]]): EducatedTransformer[Array[T], Array[T]] = {

    val colIndex = (0 until input.first.size)

    //TODO Replace with an aggregate function => optimize and make it faster (see countDistinctValsByCol_1 and countDistinctValsByCol_2 bellow)
    val distinctCountByColumn: Map[Int, Map[T, Long]] = colIndex.
      map(col => input.map(v => v(col)).countByValue.toMap).
      zipWithIndex.map(_.swap).toMap

    val finalColNames = colNames.getOrElse{
      colIndex.map(c => (c, f"*- $c%03d -*")).toMap
    }

    InputAnalyzer(Some(input.count), Some(finalColNames), Some(distinctCountByColumn))

  }

  override def transform(input: RDD[Array[T]]): RDD[Array[T]] = input

  override def transform(input: Array[T]): Array[T] = input


  def analysisReport(input: RDD[Array[T]]): Iterable[String] = {

    require(records.isDefined)
    require(colNames.isDefined)
    require(distinctCountByColumn.isDefined)

    // Any value that occurs 1 - eps0 should be ignored
    val eps0 = 1E-6
    // Any value that occurs 1 - eps0 might be ignored
    val eps1 = 1E-4

    val printableResults = distinctCountByColumn.get.toSeq.sortBy(_._1) flatMap { col =>
      val colIdx = col._1
      val counts = col._2.toSeq.sortBy(_._2).reverse
      val colHeader = f"Column: ${colIdx}%2d  ${colNames.get(colIdx)}%-20s" :: Nil
      // The value in the column that appears most often
      val heaviestRatio = counts.head._2.toDouble / records.get
      val heavyValuesStr = if ((1.0 - heaviestRatio) < eps1) {
        val highestRatioVal = counts.head
        val lowRatioVals = counts.tail
        f"| ${"Column Value"}%-20s | ${"Occurrences"}%-11s | ${"Percentage"}%-11s |" +:
          f"| ${"---------:"}%-20s | ${"----------:"}%-11s | ${"---------:"}%-11s |" +:
        f"| ${highestRatioVal._1}%20s | ${highestRatioVal._2}%10d | ${highestRatioVal._2.toDouble / records.get * 100}%10.6f |" +:
          lowRatioVals.flatMap { lrv =>
            val labels = input.filter(row => row(colIdx) == lrv._1).groupBy(row => row(41)).collect

            f"| ${" "}%20s | ${" "}%-11s | ${" "}%-11s |" +:
            f"| ${lrv._1}%20s | ${lrv._2}%11d | ${lrv._2.toDouble / records.get * 100}%11.6f |" +:
              f"| ${" "}%20s | ${"Count"}%-11s | ${"Label"}%-11s |" +:
              labels.map(lc => f"| ${" "}%20s | ${lc._2.size}%11d | ${lc._1}%-11s |")
          }
      } else Nil

      val ignorable =
        if ((1.0 - heaviestRatio) < eps0) "- This column should be ignored !!!" :: Nil
        else if ((1.0 - heaviestRatio) < eps1) "- This column might be ignored." :: Nil
        else "- This column seems ok." :: Nil


      colHeader ++ ignorable ++ heavyValuesStr
    }

    val legendStr = "" ::
      (f"| Occurrences ||") ::
      (f"| ---------- | -------------------------------- |") ::
      (f"| ${(1-eps0)*100}%10.6f | If a column value appears more often the column should probably be ignored |") ::
      (f"| ${(1-eps1)*100}%10.6f | If a column value appears more often the column might probably be ignored  |") ::
      Nil

    printableResults ++ legendStr
  }

  /**
    * Count distinct values in an RDD of Arrays of T
    *
    * @param input
    * @tparam T
    * @return
    */
  def countDistinctValsByCol_1[T](input: RDD[Array[T]]) = {

    //TODO SOOO SLOWWWWW
    val zero: Seq[List[T]] = (1 to input.first.size).map(_ => List[T]())

    def countByKey[T](in: List[T]): List[(T, Int)] =
      in.map((_, 1)).groupBy(_._1).
        map { case (s, c) => (s, c.size) }.toList

    input.
      aggregate(zero)(
        { (acc, row) => acc.zip(row).
          map { case (all, x) => x +: all }
        }, {
          (c1, c2) =>
            c1.zip(c2).map(x => x._1 ++ x._2)
        }
      ).map(countByKey)
  }

  /**
    *
    * @param input
    * @tparam T
    * @return
    */
  def countDistinctValsByCol_2[T](input: RDD[Array[T]]) = {

    //TODO SOOO SLOWWWWW
    val zero: Seq[List[(T, Int)]] = (1 to input.first.size).map(_ => List[(T, Int)]())

    def countByKey[T](in: List[(T, Int)]): List[(T, Int)] =
      in.groupBy(_._1).
        map { case (s, c) => (s, c.map(_._2).sum) }.toList

    input.
      aggregate(zero)(
        { (acc, row) => row.zip(acc).
          map { case (x, all) => (x, 1) :: all }
        }, {
          (c1, c2) =>
            c1.zip(c2).map(x => x._1 ++ x._2).
              map(countByKey(_))
        }
      )
  }

}
