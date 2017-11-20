package com.whisk.finagle.mysql

import com.twitter.finagle.mysql.Parameter.NullParameter
import com.twitter.finagle.mysql.{CanBeParameter, Parameter}

import scala.language.higherKinds

trait QueryParameter {

  def params: Seq[Parameter]
  def appendPlaceholders(stringBuilder: StringBuilder): Unit
}

class SingleParameter(val param: Parameter) extends QueryParameter {

  override val params: Seq[Parameter] = Seq(param)
  override def appendPlaceholders(stringBuilder: StringBuilder): Unit =
    stringBuilder.append("?")
}

class TupleParameter(val singleParams: Seq[SingleParameter]) extends QueryParameter {

  override val params: Seq[Parameter] = singleParams collect {
    case null => NullParameter
    case p    => p.param
  }

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

class TuplesParameter(val tuples: Seq[TupleParameter]) extends QueryParameter {

  override val params: Seq[Parameter] = tuples.flatMap(_.params)

  override def appendPlaceholders(stringBuilder: StringBuilder): Unit = {
    if (params.nonEmpty) {
      tuples.foreach { param =>
        stringBuilder.append("(")
        param.appendPlaceholders(stringBuilder)
        stringBuilder.append("),")
      }
      stringBuilder.length -= 1
    }
  }
}

object QueryParameter {

  final implicit def parameterIsSingleParameter[T](value: T)(
      implicit ev$1: T => Parameter): SingleParameter = {
    new SingleParameter(value)
  }

  final implicit def iterableOfParametersIsQueryParameter[T](values: Seq[T])(
      implicit ev: T => SingleParameter): TupleParameter = {
    new TupleParameter(values.map(v => v: SingleParameter))
  }

  final implicit def setOfParametersIsQueryParameter[T](values: Set[T])(
      implicit ev: T => SingleParameter): TupleParameter = {
    new TupleParameter(values.map(v => v: SingleParameter).toSeq)
  }

  final implicit def fromTuples[T](seq: Seq[TupleParameter])(
      implicit ev$1: T => TupleParameter): TuplesParameter = {
    new TuplesParameter(seq)
  }
}
