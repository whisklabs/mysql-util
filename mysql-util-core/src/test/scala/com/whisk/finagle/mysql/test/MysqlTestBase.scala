package com.whisk.finagle.mysql.test

import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{CountDownLatch, TimeUnit}

import com.twitter.finagle.Mysql
import com.twitter.finagle.mysql.Client
import com.twitter.finagle.stats.NullStatsReceiver
import com.twitter.finagle.tracing.NullTracer
import com.twitter.util._
import org.jdbi.v3.core.Jdbi
import org.scalatest.Suite

import scala.io.Source

trait MysqlTestBase extends DockerMysqlService { self: Suite =>

  var mysqlClient: AtomicReference[Client] = new AtomicReference[Client]()

  def schemaPaths: Seq[String] = Seq("/schema/tables.ddl")

  protected val mysqlClientLatch = new CountDownLatch(1)

  override def afterStart(): Unit = {
    super.afterStart()
    val port = mysqlContainer.mappedPorts()(MysqlAdvertisedPort)

    FuturePool.unboundedPool {
      mysqlClient.set(createClient())
      mysqlClientLatch.countDown()
    }

    val createSchemas: Seq[String] = schemaPaths.map { p =>
      Source.fromInputStream(this.getClass.getResourceAsStream(p)).mkString
    }

    if (createSchemas.nonEmpty) {
      val jdbcUrl = s"jdbc:mysql://${dockerClient.getHost}:$port/test"
      val jdbi = Jdbi.create(jdbcUrl, MysqlUser, MysqlPassword)
      createSchemas.foreach { s =>
        jdbi.withHandle { h =>
          h.createScript(s).execute()
        }
      }
      println("schema created")
    }

    mysqlClientLatch.await(20, TimeUnit.SECONDS)
  }

  protected def createClient(): Client = {
    val host = dockerClient.getHost
    val port = mysqlContainer.mappedPorts()(MysqlAdvertisedPort)
    Mysql.client
      .withCredentials(MysqlUser, MysqlPassword)
      .withDatabase(MysqlDatabase)
      .withTracer(NullTracer)
      .withStatsReceiver(NullStatsReceiver)
      .withMonitor(NullMonitor)
      .newRichClient(s"$host:$port")
  }
}
