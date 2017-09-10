package com.whisk.finagle.mysql

import java.sql.{Date, Timestamp}

import com.twitter.finagle.mysql._

import scala.reflect.ClassTag

trait ValueDecoder[T] {

  def unapply(v: Value): Option[T]
}

object ValueDecoder {

  def apply[T: ValueDecoder]: ValueDecoder[T] = implicitly[ValueDecoder[T]]

  def instance[T](conv: Value => Option[T]): ValueDecoder[T] = new ValueDecoder[T] {
    override def unapply(v: Value): Option[T] = conv(v)
  }

  def fromDirect[A <: Value : ClassTag, B](f: A => B): ValueDecoder[B] = {
    ValueDecoder.instance {
      case v: A => Some(f(v))
      case EmptyValue => None
      case NullValue => None
    }
  }

  implicit def timestamp(implicit tsValue: TimestampValue): ValueDecoder[Timestamp] =
    instance(tsValue.unapply)

  implicit val date: ValueDecoder[Date] = instance(DateValue.unapply)

  implicit val bigDecimal: ValueDecoder[BigDecimal] = instance(BigDecimalValue.unapply)

  implicit val byte: ValueDecoder[Byte] = fromDirect[ByteValue, Byte](_.b)

  implicit val short: ValueDecoder[Short] = fromDirect[ShortValue, Short](_.s)

  implicit val int: ValueDecoder[Int] = fromDirect[IntValue, Int](_.i)

  implicit val long: ValueDecoder[Long] = fromDirect[LongValue, Long](_.l)

  implicit val bigInt: ValueDecoder[BigInt] = fromDirect[BigIntValue, BigInt](_.bi)

  implicit val float: ValueDecoder[Float] = fromDirect[FloatValue, Float](_.f)

  implicit val double: ValueDecoder[Double] = fromDirect[DoubleValue, Double](_.d)

  implicit val string: ValueDecoder[String] = fromDirect[StringValue, String](_.s)
}
