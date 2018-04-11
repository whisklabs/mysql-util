package com.whisk.finagle.mysql

import java.sql.Timestamp
import java.time.Instant
import java.util.TimeZone

import com.twitter.finagle.mysql.transport.MysqlBufWriter
import com.twitter.finagle.mysql.{CanBeParameter, TimestampValue, Type}

trait ParameterImplicits {

  private val defaultTsValue =
    new TimestampValue(TimeZone.getTimeZone("UTC"), TimeZone.getTimeZone("UTC"))

  implicit val instantCanBeParameter: CanBeParameter[Instant] =
    new CanBeParameter[Instant] {
      override def sizeOf(param: Instant): Int = 12

      override def typeCode(param: Instant): Short = Type.Timestamp

      override def write(writer: MysqlBufWriter, param: Instant): Unit =
        CanBeParameter.valueCanBeParameter.write(writer, defaultTsValue(Timestamp.from(param)))
    }
}

object ParameterImplicits extends ParameterImplicits
