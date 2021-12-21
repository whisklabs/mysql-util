import sbt.Keys.resolvers

val finagleRev = "21.6.0"
val circeRev = "0.13.0"

lazy val scala212 = "2.12.12"
lazy val scala213 = "2.13.6"
lazy val supportedScalaVersions = List(scala213, scala212)

lazy val commonSettings = inThisBuild(
  List(
    organization := "com.whisk",
    scalaVersion := scala213,
    version := "0.6.8",
    scalacOptions ++= Seq("-feature", "-deprecation", "-language:implicitConversions"),
//    sonatypeProfileName := "com.whisk",
    publishMavenStyle := true,
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    homepage := Some(url("https://github.com/whisklabs/mysql-util")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/whisklabs/mysql-util"),
        "scm:git:github.com/whisklabs/mysql-util.git"
      )
    ),
    developers := List(
      Developer(
        id = "viktortnk",
        name = "Viktor Taranenko",
        email = "viktortnk@gmail.com",
        url = url("https://finelydistributed.io/")
      )
    ),
    credentials += Credentials(Path.userHome / ".m2" / ".credentials"),
    publishTo := Some(
      "internal.repo.write" at "https://nexus.whisk-dev.com/repository/whisk-maven2/"
    ),
    resolvers += "internal.repo.read" at "https://nexus.whisk-dev.com/repository/whisk-maven-group/"
    //    publishTo := Some(Opts.resolver.sonatypeStaging)
  )
)

lazy val root = project
  .in(file("."))
  .settings(commonSettings)
  .settings(
    publish := {},
    publishLocal := {},
    packagedArtifacts := Map.empty,
    crossScalaVersions := Nil
  )
  .aggregate(core, testing, circe)

lazy val core = project
  .in(file("mysql-util-core"))
  .settings(
    name := "mysql-util-core",
    commonSettings,
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies ++= Seq(
      "com.twitter" %% "finagle-mysql" % finagleRev
    )
  )
  .dependsOn(testing % Test)

lazy val testing = project
  .in(file("mysql-util-testing"))
  .settings(
    name := "mysql-util-testing",
    commonSettings,
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies ++= Seq(
      "com.twitter" %% "finagle-mysql" % finagleRev,
      "com.whisk" %% "docker-testkit-scalatest" % "0.10.0-beta8",
      "org.jdbi" % "jdbi3-core" % "3.2.0",
      "mysql" % "mysql-connector-java" % "8.0.27"
    )
  )

lazy val circe = project
  .in(file("mysql-util-circe"))
  .settings(
    name := "mysql-util-circe",
    commonSettings,
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % circeRev,
      "io.circe" %% "circe-parser" % circeRev
    )
  )
  .dependsOn(core % "compile->compile;test->test")
