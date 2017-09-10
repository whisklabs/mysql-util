package com.whisk.finagle

import com.twitter.finagle.mysql.Row

package object mysql {

  trait RowUtils {
    def getOption[T](name: String)(implicit decoder: ValueDecoder[T]): Option[T]
    def getOption[T](index: Int)(implicit decoder: ValueDecoder[T]): Option[T]
    def get[T](name: String)(implicit decoder: ValueDecoder[T]): T
    def get[T](index: Int)(implicit decoder: ValueDecoder[T]): T
    def getOrElse[T](name: String, default: => T)(implicit decoder: ValueDecoder[T]): T
    def getOrElse[T](index: Int, default: => T)(implicit decoder: ValueDecoder[T]): T
  }

  implicit class RichRow(row: Row) extends RowUtils {

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
