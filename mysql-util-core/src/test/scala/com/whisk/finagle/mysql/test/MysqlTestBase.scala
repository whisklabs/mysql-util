package com.whisk.finagle.mysql.test

import com.twitter.finagle.mysql.Client
import com.whisk.finagle.mysql.testing.MysqlTestkit
import org.scalatest.Suite

trait MysqlTestBase extends MysqlTestkit with MysqlTestHelpers { self: Suite =>

  override val mysqlInitSchemaPaths = Seq("/schema/tables.ddl")

  protected lazy val client: Client = mysqlClient.get()
}
