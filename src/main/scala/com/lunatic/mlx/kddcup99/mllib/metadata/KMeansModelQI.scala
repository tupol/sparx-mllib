package com.lunatic.mlx.kddcup99.mllib.metadata

/**
  * KMeans model Quality Information (various statistics about the model and model quality)
  */
case class KMeansModelQI(modelDistanceStats: DistanceStats, distanceStatsByK: Seq[DistanceStats],
                         labelsClustersDistribution: LabelsClustersDistribution, trainingRuntime_ms: Long)

/**
  * Basic statistics about the distances (minimum, mean, maximum and sse:Sum of Squared Errors)
  * @param min
  * @param avg
  * @param max
  * @param sse
  */
case class DistanceStats(min: Double, avg: Double, max: Double, sse: Double)

/**
  *
  * @param labels
  * @param k
  * @param labelsXclusters The clusters in labelsXclusters are presumed to be ordered from 0 until k.
  *                        The rows correspond to labels, the columns correspond to clusters and the values are counts.
  */
case class LabelsClustersDistribution(labels: Array[String], k: Int,
                                      labelsXclusters: Array[Array[Long]] ) {

  import LabelsClustersDistribution._

  require(labelsXclusters.size == labels.size)
  require(labelsXclusters(0).size == k)

  private val clusters = 0 until k

  lazy val clustersXlabels: Array[Array[Long]] = transpose(labelsXclusters)

  /**
    * Total number of records for each cluster and corresponding label
    */
  lazy val totalsByCluster = clustersXlabels.map(x => x.sum)

  /**
    * Total number of records for each label and corresponding cluster
    */
  lazy val totalsByLabel = labelsXclusters.map(x => x.sum)

  /**
    * Total number of records analysed (sum of the labelsXclusters matrix)
    */
  lazy val total = totalsByCluster.sum

  lazy val entropyByCluster = clustersXlabels.map(entropy(_))

  lazy val purityByCluster = clustersXlabels.map(purity(_))


  def addLabelDistribution(label: String, countPerCluster: Array[Long]) = {
    require(countPerCluster.size == k)
    LabelsClustersDistribution(labels :+ label, k, labelsXclusters :+ countPerCluster)
  }

  override def toString(): String = {
    val tabCountHeaderStr = "### Count label per cluster" :: "" ::
      "| Label                |   Total   |" + clusters.map(ck => s"K $ck").mkString(" ", " | ", " |") ::
      "| -------------------- | --------: |" + clusters.map(ck => "--------:").mkString(" ", " | ", " |") ::
      Nil
    val tabCountStr =
      labels.zip(totalsByLabel).zip(labelsXclusters).
        sortBy(_._1._2).reverse.
        map{ case ((label, labelTotal), clusters) =>
          val countByCluster = clusters.map(x => f"${x}%9d").mkString(" | ")
          f"| ${label}%-20s | ${labelTotal}%9d | ${countByCluster} |"
        }
    val totalByKStr = totalsByCluster.map{ t => f"$t%9d" }.mkString(" ", " | ", " |")
    val totalsStr = f"| Totals               | ${total}%9d |" + totalByKStr

    val tabClustInfo = "" +: entropyByCluster.zip(purityByCluster).zip(clusters).
      map { case((entropy, purity), k) =>
        f"| $k%5d | ${entropy}%10.8f | ${purity}%10.8f | "
      }

    ((tabCountHeaderStr ++ tabCountStr :+ totalsStr) ++ tabClustInfo).mkString("\n")
  }
}

object LabelsClustersDistribution {

  def entropy(counts: Iterable[Long]) = {
    val values = counts.filter(_ > 0)
    val n: Double = values.sum
    values.map { v =>
      val p = v / n
      -p * math.log(p)
    }.sum
  }

  def purity(counts: Iterable[Long]) = {
    val values = counts.filter(_ > 0)
    val total: Double = values.sum
    if(total == 0) -1.0
    else 1.0 * counts.max / total
  }

  private[kddcup99] def transpose(matrix : Array[Array[Long]]) = {
    val transposed = Array.fill(matrix(0).size, matrix.size)(0L)
    for {
      row <- 0 until matrix.size;
      col <- 0 until matrix(0).size
    } yield {
      transposed(col)(row) = matrix(row)(col)
    }
    transposed
  }

}
