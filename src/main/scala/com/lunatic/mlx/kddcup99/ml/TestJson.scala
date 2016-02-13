package com.lunatic.mlx.kddcup99.ml

import org.apache.spark.ml.Transformer
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.param._
import org.json4s.JsonAST._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._
import org.json4s.{CustomSerializer, NoTypeHints}

/**
  *
  */
object TestJson extends App {

  import com.lunatic.mlx.kddcup99.ml.JsonHelper._

  val toFeaturesVector = new VectorAssembler().
    setInputCols(Array("test1", "test2")).
    setOutputCol("features")


//  implicit val formats = Serialization.formats(NoTypeHints) + new VectorAssemblerSerializer
  implicit val formats = Serialization.formats(NoTypeHints) +
    new TransformerSerializer[VectorAssembler] +
  new ParamPairSerializer[String] +
  new ParamPairSerializer[Array[String]]
//  implicit val formats = DefaultFormats + FieldSerializer[VectorAssembler]()

  println("---------------------------------")
  println(writePretty(toFeaturesVector))
//  println("---------------------------------")
//  println(writePretty(toFeaturesVector.params))
  println("---------------------------------")
  val r = read[VectorAssembler](writePretty(toFeaturesVector))
  println(r.explainParams())
  println("---------------------------------")
  println(toFeaturesVector.explainParams())
  println("---------------------------------")
  println(writePretty(r.params))

}




object JsonHelper {

    implicit class AParams[P <: Params]( xparams: P) extends Params {
      override def copy(extra: ParamMap): Params = xparams.copy(extra)
      override val uid: String = xparams.uid
      def setPP(pp: ParamPair[_]): P = xparams.set(pp).asInstanceOf[P]
    }

    implicit def paramToJObject(param: Param[_]): JObject =
      JObject(JField("parent", JString(param.parent)) ::
        JField("name", JString(param.name)) ::
        JField("doc", JString(param.doc)) ::
        Nil)

    implicit def paramToJField(param: Param[_]): JField =
      JField("param", param)


    class ParamSerializer extends CustomSerializer[Param[_]](format => ( {
      case JObject(JField("parent", JString(parent)) ::
        JField("name", JString(name)) ::
        JField("doc", JString(doc)) ::
        Nil) =>
        new Param(parent, name, doc)
    }, {
      case x: Param[_] => x
    }
      ))

    class ParamPairSerializer[T](implicit m: Manifest[T]) extends CustomSerializer[ParamPair[T]](format => ( {
      case JObject(JField("param", JObject(param)) ::
        JField("value", JObject(value)) ::
        Nil) =>
        new ParamPair[T](param.asInstanceOf[Param[T]], value.asInstanceOf[T])
    }, {
      case pp: ParamPair[_] => paramPairToJValue(pp)
    }
      ))

    class TransformerSerializer[T <: Transformer](implicit m: Manifest[T]) extends CustomSerializer[T](format => ( {
      case JObject(JField("class", JString(classname))
        :: JField("uid", JString(uid))
        :: JField("params", JArray(params))
        :: Nil) =>
        val newTransformer = Class.forName(classname).getConstructor(classOf[String]).newInstance(uid).asInstanceOf[T]
        println("-----------")
        println(params.getClass)
        println(params.mkString(", "))
        println("-----------")
        val transformer = params.
          map(_.asInstanceOf[ParamPair[_]]).
          foldLeft(newTransformer)((trans, param) => trans.setPP(param))
        transformer
    }, {
      case trans: Transformer =>
        val xx = trans.extractParamMap().toSeq.map(pm => paramPairToJValue(pm)).toList

        JObject(JField("class", JString(trans.getClass.getName)) ::
          JField("uid", JString(trans.uid)) ::
          JField("params", JArray(xx)) ::
          Nil)
    }
      ))

    implicit def paramPairToJValue(p: ParamPair[_]): JValue = p match {
      case ParamPair(param: Param[_], value: String) => JObject(paramToJField(param) ::
        JField("value", JString(value)) ::
        Nil)
      case ParamPair(param: LongParam, value: Long) => JObject(paramToJField(param) ::
        JField("value", JInt(value)) ::
        Nil)
      case ParamPair(param: FloatParam, value: Float) => JObject(paramToJField(param) ::
        JField("value", JDouble(value)) ::
        Nil)
      case ParamPair(param: DoubleParam, value: Double) => JObject(paramToJField(param) ::
        JField("value", JDouble(value)) ::
        Nil)
      case ParamPair(param: BooleanParam, value: Boolean) => JObject(paramToJField(param) ::
        JField("value", JBool(value)) ::
        Nil)
      case ParamPair(param: StringArrayParam, value: Array[String]) => JObject(paramToJField(param) ::
        JField("value", JArray(value.map(JString(_)).toList)) ::
        Nil)
      case ParamPair(param: IntArrayParam, value: Array[Int]) => JObject(paramToJField(param) ::
        JField("value", JArray(value.map(JInt(_)).toList)) ::
        Nil)
      case ParamPair(param: DoubleArrayParam, value: Array[Double]) => JObject(paramToJField(param) ::
        JField("value", JArray(value.map(JDouble(_)).toList)) ::
        Nil)
      case _ => JNothing
    }

}

class VectorAssemblerSerializer extends CustomSerializer[VectorAssembler](format => ( {
  case JObject(JField("uid", JString(uid)) :: JField("inputCols", JArray(inputCols))
    :: JField("outputCol", JString(outputCol)) :: Nil) =>
    new VectorAssembler(uid).
      setInputCols(inputCols.map(_.toString).toArray).
      setOutputCol(outputCol)
}, {
  case x: VectorAssembler =>
    JObject(JField("uid", JString(x.uid)) ::
      JField("inputCols", JArray(x.getInputCols.map(JString(_)).toList)) ::
      JField("outputCol", JString(x.getOutputCol)) :: Nil)
}
  ))
