package com.whisk.finagle.mysql.test

import java.util.UUID

import com.twitter.util.{Await, Future}
import com.whisk.finagle.mysql._
import com.whisk.finagle.mysql.test.Model.Recipe
import org.scalatest.{FunSuite, MustMatchers}

class DecodersIntegrationTest
    extends FunSuite
    with MustMatchers
    with MysqlTestBase
    with MysqlTestHelpers {

  private lazy val client = mysqlClient.get()

  test("decode primitives through their direct values") {

    val recipe = Recipe(UUID.randomUUID().toString, "Grilled Salmon")

    checkOk(
      client
        .prepareAndExecute("insert into recipes(id, name) values (?, ?)", recipe.id, recipe.name))

    val nameRes: Future[Seq[String]] =
      client.prepareAndQuery("select name from recipes where id = ?", recipe.id)(row =>
        row.get[String]("name"))

    nameRes.futureValue mustBe Seq(recipe.name)
  }

  test("decode rawjson strings") {
    val id = UUID.randomUUID().toString
    val name = "Lasagna"
    val data = """{"cuisine": "Italian"}"""

    checkOk(
      client.prepareAndExecute("insert into recipes(id, name, data) " +
                                 "values (?, ?, ?)",
                               id,
                               name,
                               data))

    val jsonF: Future[Seq[RawJsonString]] =
      client.prepareAndQuery("select data from recipes where id = ?", id)(row =>
        row.get[RawJsonString]("data"))

    jsonF.futureValue mustBe Seq(RawJsonString(data))
  }
}
