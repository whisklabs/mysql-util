package com.whisk.finagle.mysql

import java.nio.charset.StandardCharsets

import com.twitter.finagle.mysql.{RawValue, StringValue, Value}

case class RawJsonString(value: String)

object RawJsonJsonValue extends ValueDecoder[RawJsonString] {

  val JsonTypeCode: Short = 0xf5

  def unapply(v: Value): Option[RawJsonString] = v match {
    case RawValue(JsonTypeCode, _, _, bytes) =>
      Some(RawJsonString(new String(bytes, StandardCharsets.UTF_8)))
    case StringValue(str) => Some(RawJsonString(str))
    case _                => None
  }
}
