package com.whisk.finagle.mysql

import com.twitter.finagle.mysql.{CanBeParameter, Parameter}

import scala.language.higherKinds

trait QueryParameter {

  def params: Seq[Parameter]
  def appendPlaceholders(stringBuilder: StringBuilder): Unit
}

class SingleParameter(param: Parameter) extends QueryParameter {

  override val params: Seq[Parameter] = Seq(param)
  override def appendPlaceholders(stringBuilder: StringBuilder): Unit =
    stringBuilder.append("?")
}

class MultipleParameter(override val params: Seq[Parameter]) extends QueryParameter {
  override def appendPlaceholders(stringBuilder: StringBuilder): Unit = {
    val length = params.size
    if (length > 0) {
      stringBuilder.append("?")
      var i = 1
      while (i < length) {
        stringBuilder.append(",?")
        i += 1
      }
    }
  }
}

object QueryParameter {

  final implicit def parameterIsSingleParameter[T](value: T)(
      implicit ev: CanBeParameter[T]): QueryParameter = {
    new SingleParameter(Parameter.wrap(value)(ev))
  }

  final implicit def seqOfParametersIsQueryParameter[T, CC[X] <: Seq[X]](values: CC[T])(
      implicit ev: CanBeParameter[T]): QueryParameter = {
    new MultipleParameter(values.map(v => Parameter.wrap(v)(ev)))
  }

  final implicit def setOfParametersIsQueryParameter[T, CC[X] <: Set[X]](values: CC[T])(
      implicit ev: CanBeParameter[T]): QueryParameter = {
    seqOfParametersIsQueryParameter(values.toSeq)
  }
}
