package com.whisk.finagle.mysql

import com.twitter.finagle.mysql.{Charset, RawValue, StringValue, Value}

case class RawJsonString(value: String)

object RawJsonJsonValue extends ValueDecoder[RawJsonString] {

  val JsonTypeCode: Short = 0xf5

  def unapply(v: Value): Option[RawJsonString] = v match {
    case RawValue(JsonTypeCode, Charset.Binary, _, bytes) =>
      Some(RawJsonString(new String(bytes, Charset(Charset.Utf8_bin))))
    case StringValue(str) => Some(RawJsonString(str))
    case _                => None
  }
}
