package com.whisk.finagle.mysql.test

import com.twitter.finagle.mysql.{OK, Result}
import com.twitter.util.{Await, Duration, Future}
import org.scalatest.{Assertion, MustMatchers}

trait MysqlTestHelpers extends MustMatchers {

  protected val WaitTimeout: Duration = Duration.fromSeconds(5)

  implicit class RichFuture[T](future: Future[T]) {

    def futureValue: T = Await.result(future, WaitTimeout)
  }

  protected def checkOk(result: Future[Result]): Assertion = {
    Await.result(result) mustBe an[OK]
  }
}
