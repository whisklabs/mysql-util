package com.whisk.finagle.mysql.testing

import com.whisk.docker.testkit.{ContainerSpec, DockerReadyChecker, ManagedContainers}
import com.whisk.docker.testkit.scalatest.DockerTestKitForAll
import org.scalatest.Suite

import scala.concurrent.duration._

trait DockerMysqlService extends DockerTestKitForAll { self: Suite =>

  def MysqlAdvertisedPort = 3306
  val MysqlUser = "test"
  val MysqlPassword = "test"
  val MysqlDatabase = "test"

  protected val mysqlContainer = ContainerSpec("quay.io/whisk/fastboot-mysql:5.7.19")
    .withExposedPorts(MysqlAdvertisedPort)
    .withReadyChecker(
      DockerReadyChecker
        .Jdbc(
          driverClass = "com.mysql.cj.jdbc.Driver",
          user = MysqlUser,
          password = Some(MysqlPassword),
          database = Some(s"$MysqlDatabase?useSSL=false"),
          port = Some(MysqlAdvertisedPort)
        )
        .looped(25, 1.second)
    )
    .toContainer

  override val managedContainers: ManagedContainers = mysqlContainer.toManagedContainer
}
