package com.lunatic.mlx

import java.io._
import com.lunatic.mlx.kddcup99.mllib.Configuration
import com.lunatic.mlx.kddcup99.mllib.metadata.KMeansParams
import org.apache.commons.io.FileUtils
import org.apache.hadoop.conf.{Configuration => HadoopConfiguration}
import org.apache.hadoop.fs.{FileSystem, _}

import scala.util.Try


/**
 * Common utilities
 */
package object kddcup99 {

  def removeHdfsFile(path: String) = {
    val hdfs = FileSystem.get(new HadoopConfiguration())
    val workingPath = new Path(path)
    hdfs.delete(workingPath, true) // delete recursively
  }

  def removeLocalFile(path: String) = {
    FileUtils.deleteQuietly(new java.io.File(path))
  }


  /**
   * Save a sequence of Strings as lines in an HDFS file
    *
    * @param rez
   * @param path
   * @return
   */
  def saveLinesToFile(rez: Iterable[String], path: String, overwrite: Boolean = true) = {

    Try {
      // Create the HDFS file system handle
      val hdfs = FileSystem.get(new HadoopConfiguration())
      // Create a writer
      val writer = new PrintWriter(new BufferedOutputStream(hdfs.create(new Path(setTxtExtension(path)), overwrite)), true)
      //write each line
      rez.foreach(line => Try(writer.print(line + "\n")))
      // Close the streams
      Try(writer.close())
    }
  }

  def saveObjectToFile[T <: scala.AnyRef](that: T, path: String, overwrite: Boolean = true): Boolean = {

    Try {
      // Create the HDFS file system handle
      val hdfs = FileSystem.get(new HadoopConfiguration())
      val writer = new ObjectOutputStream(new BufferedOutputStream(hdfs.create(new Path(setBinExtension(path)), overwrite)))
      val result = Try(writer.writeObject(that))
      Try(writer.close)
      result
    }.flatten.isSuccess
  }


  def loadObjectFromFile[T](path: String)(implicit mf: Manifest[T]): Try[T] = {

    Try{
      // Create the HDFS file system handle
      val hdfs = FileSystem.get(new HadoopConfiguration())
      val reader = new ObjectInputStream(new BufferedInputStream(hdfs.open(new Path(setBinExtension(path)))))
      val result = Try(reader.readObject().asInstanceOf[T])
      Try(reader.close)
      result
    }.flatten
  }

  /**
   * Nothing fancy.
   *
   * Beware of maps of maps...!
   *
   * @param that
   * @param path
   * @tparam T
   * @return
   */
  def saveObjectToJsonFile[T <: scala.AnyRef](that: T, path: String, overwrite: Boolean = true)(implicit mf: Manifest[T]): Boolean = {
    import org.json4s._
    import org.json4s.jackson.Serialization._
    implicit val formats = DefaultFormats + FieldSerializer[T]()

    Try {
      // Create the HDFS file system handle
      val hdfs = FileSystem.get(new HadoopConfiguration())
      val writer = new PrintWriter(new BufferedOutputStream(hdfs.create(new Path(setJsonExtension(path)), overwrite)), true)
      val result = Try(writePretty(that, writer))
      Try(writer.close)
      result
    }.flatten.isSuccess
  }

  /**
   * Nothing fancy.
   *
   * Beware of maps of maps...!
   *
   * @param path
   * @param mf
   * @tparam T
   * @return
   */
  def loadObjectFromJsonFile[T](path: String)(implicit mf: Manifest[T]): Try[T] = {

    import org.json4s._
    import org.json4s.jackson.Serialization._
    implicit val formats = DefaultFormats + FieldSerializer[T]()

    Try{
      // Create the HDFS file system handle
      val hdfs = FileSystem.get(new HadoopConfiguration())
      val reader = new InputStreamReader(new BufferedInputStream(hdfs.open(new Path(setJsonExtension(path)))))
      val result = Try(read[T](reader))
      Try(reader.close)
      result
    }.flatten
  }



  /**
   * Create a stub file that can be used to save models and results
    *
    * @param params
   * @param appConf
   * @return
   */
  def stubFilePath(params: KMeansParams)(implicit appConf: Configuration) =
    f"${appConf.outputPath}/kmeans_${params.experimentId}"


  private def setJsonExtension(path: String) = setExtension(".json", path)

  private def setBinExtension(path: String) = setExtension(".bin", path)

  private def setTxtExtension(path: String) = setExtension(".txt", path)

  private def setExtension(ext: String, path: String) =
    if (path.endsWith(ext)) path
    else path + ext

  /**
   * Run a block and return the block result and the runtime in millis
    *
    * @param block
   * @return
   */
  def timeCode[T](block: => T): (T, Long) = {
    val start = new java.util.Date
    val result = block
    val runtime = (new java.util.Date).toInstant.toEpochMilli - start.toInstant.toEpochMilli
    (result, runtime)
  }


  /**
   * input parameters for training different KMeans models
    *
    * @param argument -> start,stop,step
   * @return
   */
  def parseStringToRange(argument: String): List[Int] = {
    val arg = argument.split(",").map(_.toInt)
    val (start, stop, step) = (arg.head, arg(1), arg(2))
    (start until (stop + step) by step).toList
  }

}
