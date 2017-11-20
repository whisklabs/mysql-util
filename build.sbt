val finagleRev = "17.11.0"

lazy val commonSettings = inThisBuild(
  List(
    organization := "com.whisk",
    scalaVersion := "2.12.4",
    version := "0.2.1",
    scalacOptions ++= Seq("-feature", "-deprecation", "-language:implicitConversions"),
    sonatypeProfileName := "com.whisk",
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
      Developer(id = "viktortnk",
                name = "Viktor Taranenko",
                email = "viktortnk@gmail.com",
                url = url("https://finelydistributed.io/"))
    ),
    publishTo := Some(Opts.resolver.sonatypeStaging)
  ))

lazy val root = project
  .in(file("."))
  .settings(commonSettings)
  .settings(publish := {}, publishLocal := {}, packagedArtifacts := Map.empty)
  .aggregate(core, testing, circe)

lazy val core = project
  .in(file("mysql-util-core"))
  .settings(
    name := "mysql-util-core",
    commonSettings,
    libraryDependencies ++= Seq(
      "com.twitter" %% "finagle-mysql" % finagleRev,
    )
  )
  .dependsOn(testing % Test)

lazy val testing = project
  .in(file("mysql-util-testing"))
  .settings(
    name := "mysql-util-testing",
    commonSettings,
    libraryDependencies ++= Seq(
      "com.twitter" %% "finagle-mysql" % finagleRev,
      "com.whisk" %% "docker-testkit-scalatest" % "0.10.0-beta3",
      "org.jdbi" % "jdbi3" % "3.0.0-beta2",
      "mysql" % "mysql-connector-java" % "5.1.44"
    )
  )

lazy val circe = project
  .in(file("mysql-util-circe"))
  .settings(
    name := "mysql-util-circe",
    commonSettings,
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % "0.9.0-M2",
      "io.circe" %% "circe-parser" % "0.9.0-M2"
    )
  )
  .dependsOn(core % "compile->compile;test->test")
