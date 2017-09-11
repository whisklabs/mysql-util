package com.whisk.finagle.mysql.circe

import java.util.UUID

import com.twitter.finagle.mysql.Row
import com.whisk.finagle.mysql._
import com.whisk.finagle.mysql.test.Model.{Recipe, RecipeMetadata}
import com.whisk.finagle.mysql.test.{MysqlTestBase, MysqlTestHelpers}
import io.circe.syntax._
import io.circe._
import org.scalatest.{FunSuite, MustMatchers}

class CirceDecodersTest
    extends FunSuite
    with MustMatchers
    with MysqlTestBase
    with MysqlTestHelpers {

  private lazy val client = mysqlClient.get()

  test("decode circe types") {

    val recipe = Recipe(UUID.randomUUID().toString, "Salmon pasta salad with lemon & capers")

    val json = Json.obj("mealType" := "Lunch")

    checkOk(
      client
        .prepareAndExecute("insert into recipes(id, name, data) values (?, ?, ?)",
                           recipe.id,
                           recipe.name,
                           json.noSpaces))

    val row: Row = client
      .prepareAndQuery("select data from recipes where id = ?", recipe.id)(identity)
      .map(_.head)
      .futureValue

    row.get[Json]("data") mustEqual json
    row.get[JsonObject]("data") mustEqual json.asObject.get

    val expectedValue = RecipeMetadata(mealType = Some("Lunch"))

    implicit val metadataCirceDecoder: Decoder[RecipeMetadata] =
      Decoder.forProduct2("cuisine", "mealType")(RecipeMetadata.apply)

    row.json[RecipeMetadata]("data") mustBe expectedValue
  }

}
