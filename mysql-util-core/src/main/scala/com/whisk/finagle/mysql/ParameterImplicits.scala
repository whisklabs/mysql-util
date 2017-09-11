package com.whisk.finagle.mysql

import com.twitter.finagle.mysql.{CanBeParameter, Parameter}

trait ParameterImplicits {

  implicit def optionIsParameter[T](value: Option[T])(
      implicit _evidence: CanBeParameter[T]): Parameter = {
    value match {
      case Some(x) => Parameter.wrap(x)
      case None    => Parameter.NullParameter
    }
  }
}

object ParameterImplicits extends ParameterImplicits
