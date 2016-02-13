package com.lunatic.spring

import scala.collection.JavaConversions._

/**
  *
  */
object ListConverter {

  def toScala( jlist: java.util.List[AnyRef] ) = {
    jlist.toList
  }

}
