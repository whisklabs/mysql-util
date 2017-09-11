package com.whisk.finagle.mysql.circe

import com.twitter.finagle.mysql.{Row, Value}
import io.circe.Decoder

trait CirceRowImplicits {

  implicit class CirceRichRow(row: Row) extends CirceRowOps {

    private def fromValue[T](value: Value)(implicit ev: Decoder[T]): Option[T] = {
      CirceJsonDecoder
        .unapply(value)
        .map(
          _.as[T].fold(
            failure => throw failure, //TODO better handing for failure
            identity
          ))
    }

    override def jsonOption[T](name: String)(implicit decoder: Decoder[T]): Option[T] = {
      row.apply(name).flatMap(fromValue[T])
    }

    override def jsonOption[T](index: Int)(implicit decoder: Decoder[T]): Option[T] =
      fromValue[T](row.values(index))

    override def json[T](name: String)(implicit decoder: Decoder[T]): T =
      jsonOption[T](name).getOrElse(null.asInstanceOf[T])

    override def json[T](index: Int)(implicit decoder: Decoder[T]): T =
      jsonOption[T](index).getOrElse(null.asInstanceOf[T])

    override def jsonOrElse[T](name: String, default: => T)(implicit decoder: Decoder[T]): T = {
      jsonOption[T](name).getOrElse(default)
    }

    override def jsonOrElse[T](index: Int, default: => T)(implicit decoder: Decoder[T]): T = {
      jsonOption[T](index).getOrElse(default)
    }

  }
}

object CirceRowImplicits extends CirceRowImplicits
