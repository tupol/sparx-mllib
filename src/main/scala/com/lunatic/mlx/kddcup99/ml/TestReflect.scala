package com.lunatic.mlx.kddcup99.ml

import org.apache.spark.ml.Transformer

/**
  *
  */
object TestReflect extends App {

//  def createInstance[T](name : String, arg: String): T = {
//
//    Class.forName(name).getConstructor(Class[String]).newInstance(arg).asInstanceOf[T]
//
//  }
//
//  createInstance[String]("String", "abc")

  val v = "org.apache.spark.ml.feature.VectorAssembler"

//  println(java.lang.Class.forName(v))
//    .getConstructor(Class[String])
  java.lang.Class.forName(v).getConstructors.foreach(println)
  java.lang.Class.forName(v).getConstructors.map{ c =>
    println(s"${c.getClass} ${c}")
    if(c.getParameterCount == 0) c.newInstance()
    else {
      println( s"  ${c.getParameters.apply(0)}")
      c.newInstance("test")
    }
  }
  println(Class.forName(v).getConstructor(classOf[java.lang.String]).newInstance("dd"))
  println(java.lang.Class.forName(v).getConstructor().newInstance().asInstanceOf[Transformer])
}
