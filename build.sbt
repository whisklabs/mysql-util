import Dependencies._

lazy val commonSettings = inThisBuild(
  List(
    organization := "com.whisk",
    scalaVersion := "2.12.3",
    version := "0.0.1",
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
  .aggregate(core)

lazy val core = project
  .in(file("mysql-util-core"))
  .settings(
    name := "mysql-util-core",
    commonSettings,
    libraryDependencies ++= Seq(finagleMysql, scalaTest % Test)
  )
