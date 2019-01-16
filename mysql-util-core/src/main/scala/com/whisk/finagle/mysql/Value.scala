package com.whisk.finagle.mysql

import com.twitter.finagle.mysql.{MysqlCharset, RawValue, StringValue, Value}

case class RawJsonString(value: String)

object RawJsonJsonValue extends ValueDecoder[RawJsonString] {

  val JsonTypeCode: Short = 0xf5

  def unapply(v: Value): Option[RawJsonString] = v match {
    case RawValue(JsonTypeCode, MysqlCharset.Binary, _, bytes) =>
      Some(RawJsonString(new String(bytes, MysqlCharset(MysqlCharset.Utf8_bin))))
    case StringValue(str) => Some(RawJsonString(str))
    case _                => None
  }
}
