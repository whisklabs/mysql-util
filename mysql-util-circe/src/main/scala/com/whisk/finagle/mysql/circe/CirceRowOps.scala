package com.whisk.finagle.mysql.circe

import io.circe.Decoder

trait CirceRowOps {

  def jsonOption[T](name: String)(implicit decoder: Decoder[T]): Option[T]
  def jsonOption[T](index: Int)(implicit decoder: Decoder[T]): Option[T]
  def json[T](name: String)(implicit decoder: Decoder[T]): T
  def json[T](index: Int)(implicit decoder: Decoder[T]): T
  def jsonOrElse[T](name: String, default: => T)(implicit decoder: Decoder[T]): T
  def jsonOrElse[T](index: Int, default: => T)(implicit decoder: Decoder[T]): T
}
