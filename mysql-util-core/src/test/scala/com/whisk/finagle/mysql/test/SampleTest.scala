package com.whisk.finagle.mysql.test

import com.twitter.finagle.mysql.OK
import com.twitter.util.Await
import org.scalatest.{FunSuite, MustMatchers}

class SampleTest extends FunSuite with MysqlTestBase with MustMatchers {

  private lazy val client = mysqlClient.get()

  test("mock test") {
    Await.result(client.ping()) mustBe an [OK]
  }
}
