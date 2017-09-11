package com.whisk.finagle.mysql

import com.twitter.finagle.mysql.{Client, Parameter, Result, Row}
import com.twitter.util.Future

trait MysqlClientImplicits {

  implicit class RichClient(client: Client) {

    def prepareAndExecute(sql: String, params: Parameter*): Future[Result] = {
      client.prepare(sql).apply(params: _*)
    }

    def prepareAndQuery[T](sql: String, params: Parameter*)(mapper: Row => T): Future[Seq[T]] = {
      client.prepare(sql).select[T](params: _*)(mapper)
    }
  }

}

object MysqlClientImplicits extends MysqlClientImplicits