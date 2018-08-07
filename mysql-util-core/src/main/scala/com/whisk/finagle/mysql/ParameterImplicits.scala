package com.whisk.finagle.mysql

import java.sql.Timestamp
import java.time.Instant

import com.twitter.finagle.mysql.transport.MysqlBufWriter
import com.twitter.finagle.mysql.{CanBeParameter, TimestampValue, Type}

trait ParameterImplicits {

  implicit val instantCanBeParameter: CanBeParameter[Instant] =
    new CanBeParameter[Instant] {
      override def sizeOf(param: Instant): Int = 12

      override def typeCode(param: Instant): Short = Type.Timestamp

      override def write(writer: MysqlBufWriter, param: Instant): Unit =
        CanBeParameter.valueCanBeParameter.write(writer, TimestampValue(Timestamp.from(param)))
    }
}

object ParameterImplicits extends ParameterImplicits
