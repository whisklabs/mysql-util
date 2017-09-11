package com.whisk.finagle.mysql.circe

import com.twitter.finagle.mysql.Value
import com.whisk.finagle.mysql.{RawJsonString, ValueDecoder}
import io.circe.{Decoder, Json, JsonObject}
import io.circe.parser._

object CirceJsonDecoder extends ValueDecoder[Json] {
  override def unapply(v: Value): Option[Json] = {
    ValueDecoder.rawJsonString.unapply(v).map {
      case RawJsonString(jsonStr) =>
        parse(jsonStr).fold(
          failure => throw failure, //TODO better handing for failure
          identity
        )
    }
  }
}

trait CirceValueDecoders {

  final implicit val circeJsonDecoder: ValueDecoder[Json] = CirceJsonDecoder

  final implicit val circeJsonObjectDecoder: ValueDecoder[JsonObject] = {
    CirceJsonDecoder.map(
      _.as[JsonObject].fold(
        failure => throw failure, //TODO better handing for failure
        identity
      ))
  }
}

object CirceValueDecoders extends CirceValueDecoders
