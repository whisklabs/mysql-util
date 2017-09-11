package com.whisk.finagle.mysql

trait RowOps {

  def getOption[T](name: String)(implicit decoder: ValueDecoder[T]): Option[T]
  def getOption[T](index: Int)(implicit decoder: ValueDecoder[T]): Option[T]
  def get[T](name: String)(implicit decoder: ValueDecoder[T]): T
  def get[T](index: Int)(implicit decoder: ValueDecoder[T]): T
  def getOrElse[T](name: String, default: => T)(implicit decoder: ValueDecoder[T]): T
  def getOrElse[T](index: Int, default: => T)(implicit decoder: ValueDecoder[T]): T
}
