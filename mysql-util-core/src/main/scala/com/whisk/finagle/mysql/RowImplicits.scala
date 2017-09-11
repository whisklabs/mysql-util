package com.whisk.finagle.mysql

import com.twitter.finagle.mysql.Row

trait RowImplicits {

  implicit class RichRow(row: Row) extends RowOps {

    override def getOption[T](name: String)(implicit decoder: ValueDecoder[T]): Option[T] = {
      row.apply(name).flatMap(decoder.unapply)
    }

    override def getOption[T](index: Int)(implicit decoder: ValueDecoder[T]): Option[T] =
      decoder.unapply(row.values(index))

    override def get[T](name: String)(implicit decoder: ValueDecoder[T]): T =
      getOption[T](name).getOrElse(null.asInstanceOf[T])

    override def get[T](index: Int)(implicit decoder: ValueDecoder[T]): T =
      getOption[T](index).getOrElse(null.asInstanceOf[T])

    override def getOrElse[T](name: String, default: => T)(implicit decoder: ValueDecoder[T]): T = {
      getOption[T](name).getOrElse(default)
    }

    override def getOrElse[T](index: Int, default: => T)(implicit decoder: ValueDecoder[T]): T = {
      getOption[T](index).getOrElse(default)
    }
  }
}
