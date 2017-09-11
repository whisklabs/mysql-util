package com.whisk.finagle.mysql.circe

import java.util.UUID

import com.twitter.util.Future
import com.whisk.finagle.mysql._
import com.whisk.finagle.mysql.test.Model.Recipe
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

  test("decode circe Json and JsonObject types") {

    val recipe = Recipe(UUID.randomUUID().toString, "Grilled Salmon")

    val json = Json.obj("cuisine" := "Italian")

    checkOk(
      client
        .prepareAndExecute("insert into recipes(id, name, data) values (?, ?, ?)",
                           recipe.id,
                           recipe.name,
                           json.noSpaces))

    val jsonF: Future[Json] =
      client
        .prepareAndQuery("select data from recipes where id = ?", recipe.id)(row =>
          row.get[Json]("data"))
        .map(_.head)

    jsonF.futureValue mustBe json
  }

}
