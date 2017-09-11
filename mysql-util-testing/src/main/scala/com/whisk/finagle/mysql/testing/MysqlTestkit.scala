package com.whisk.finagle.mysql.testing

import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{CountDownLatch, TimeUnit}

import com.twitter.finagle.Mysql
import com.twitter.finagle.mysql.Client
import com.twitter.finagle.stats.NullStatsReceiver
import com.twitter.finagle.tracing.NullTracer
import com.twitter.util._
import org.jdbi.v3.core.Jdbi
import org.scalatest.Suite
import org.slf4j.LoggerFactory

import scala.io.Source

trait MysqlTestkit extends DockerMysqlService { self: Suite =>

  private val log = LoggerFactory.getLogger(classOf[MysqlTestkit])

  protected val mysqlClient: AtomicReference[Client] = new AtomicReference[Client]()

  protected val mysqlClientLatch = new CountDownLatch(1)

  def mysqlInitSchemaPaths: Seq[String] = Seq()

  override def afterStart(): Unit = {
    super.afterStart()
    val port = mysqlContainer.mappedPorts()(MysqlAdvertisedPort)

    def initClient(): Unit = {
      mysqlClient.set(createClient())
      mysqlClientLatch.countDown()
    }

    // this block here is for performance optimisations of bootstrapping test
    // as Finagle Mysql client takes a while to init
    if (mysqlInitSchemaPaths.isEmpty) {
      // no need to create schemas -> initialising client in current thread
      initClient()
    } else {
      // need to create schemas
      log.info(s"initialising schemas for paths:")
      mysqlInitSchemaPaths.foreach(p => log.info("  - " + p))

      val createSchemas: Seq[String] = mysqlInitSchemaPaths.map { p =>
        Source.fromInputStream(this.getClass.getResourceAsStream(p)).mkString
      }

      log.info("all schema files loaded")

      // start initialising Finagle Mysql client in separate thread
      FuturePool.unboundedPool {
        initClient()
      }

      val jdbcUrl = s"jdbc:mysql://${dockerClient.getHost}:$port/test"
      val jdbi = Jdbi.create(jdbcUrl, MysqlUser, MysqlPassword)
      createSchemas.foreach { s =>
        jdbi.withHandle { h =>
          h.createScript(s).execute()
        }
      }

      log.info("schemas created")
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
