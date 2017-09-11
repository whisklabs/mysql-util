package com.whisk.finagle.mysql.circe

import com.whisk.finagle.mysql.{RawJsonString, ValueDecoder}
import io.circe.{Json, JsonObject}
import io.circe.parser._

trait CirceValueDecoders {

  implicit val jsonDecoder: ValueDecoder[Json] = {
    ValueDecoder.rawJsonString.map {
      case RawJsonString(value) =>
        parse(value).fold(
          failure => throw failure, //TODO better handing for failure
          identity
        )
    }
  }

  implicit val jsonObjectDecoder: ValueDecoder[JsonObject] = {
    jsonDecoder.map(
      _.as[JsonObject].fold(
        failure => throw failure, //TODO better handing for failure
        identity
      ))
  }
}

object CirceValueDecoders extends CirceValueDecoders
