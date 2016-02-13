package com.lunatic.mlx.kddcup99.ml

import org.apache.spark.ml.Transformer
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.param.{Param, ParamMap, ParamPair, Params}

import scala.util.{Success, Try}
import scala.xml.Elem


/**
  *
  */
object TestXML extends App {


  implicit class XParams[P <: Params](xparams: P) extends Params {
    override def copy(extra: ParamMap): Params = xparams.copy(extra)

    override val uid: String = xparams.uid

    def setParamPair(pp: ParamPair[_]): P = xparams.set(pp).asInstanceOf[P]
  }


  def valueToXml[T](value: T): Seq[Elem] = {

    val typeStr = if(value.getClass.isArray) value.getClass.getComponentType.getName
                  else value.getClass.getName
    value match {
      case a: Array[_]  => a.map{ elem => <value class={typeStr}>{elem}</value> }
      case x            => Seq(<value class={typeStr}>{x}</value>)
    }
  }


  implicit def toXml[T](paramPair: ParamPair[T])(implicit m: Manifest[T]): Elem = {
    <paramPair>
      <param class={paramPair.param.getClass.getTypeName}>
        <parent>
          {paramPair.param.parent}
        </parent>
        <name>
          {paramPair.param.name}
        </name>
        <doc>
          {paramPair.param.doc}
        </doc>
      </param>
      {valueToXml(paramPair.value)}
    </paramPair>
  }

  implicit def toXml[T <: Transformer](transformer: T): Elem = {
    val params = transformer.extractParamMap().toSeq.map(toXml(_)).theSeq
    <transformer uid={transformer.uid} class={transformer.getClass.getName}>
      {params}
    </transformer>
  }


  def fromXml(node: scala.xml.Node): Try[Transformer] = {

    println("============================")

    val classname = (node \ "@class").text
    val uid = (node \ "@uid").text
    val newTransformer = Class.forName(classname).
      getConstructor(classOf[String]).
      newInstance(uid).asInstanceOf[Transformer]


    val params = (node \ "paramPair").theSeq.map { node =>
//      println("-----------")
      val param = node \ "param"
      val paramClassName = (param \ "@class").text
      val parentStr = (param \ "parent").text
      val name = (param \ "name").text
      val doc = (param \ "doc").text
      val valueNodes = (node \ "value")

      val paramClass = Class.forName(paramClassName)

      val acceptableCostructors = paramClass.getConstructors.
        filter(_.getParameterCount == 3)

      // TODO there must be a better way
      val instances = acceptableCostructors.
        map { constructor =>
          Try(constructor.newInstance(parentStr, name, doc).asInstanceOf[Param[_]]) orElse
            Try(constructor.newInstance(newTransformer, name, doc).asInstanceOf[Param[_]])
        }.
        filter(_.isSuccess)

      println(instances.size)

      instances match {
        case Success(inst) +: rest => Some(inst)
        case _ => None
      }

//      valueNodes.toSeq match {
//        case one :: Nil =>
//        case many : Seq =>
//      }

      val values = valueNodes.toSeq.map{ entry =>
        val valueClassName = (entry \ "class").text
//      println(s"- ${paramClassName}")
      println(s"  ${valueClassName}")
//      println(valueNode.toSeq.map(_.text))

      }
    }

//    println(newTransformer.getClass.getName)
//
    Try(newTransformer)
  }

  val tr = new VectorAssembler().
    setInputCols(Array("test1", "test2", "test3")).
    setOutputCol("features")

//  println(toXml(tr))

  println(fromXml(toXml(tr)))


  //  tr.extractParamMap().toSeq.foreach{pp =>
  ////    println(XmlHelper.toXml(pp))
  ////    XmlHelper.toXml(pp)
  //    fromXml(XmlHelper.toXml(pp))
  //  }

}

