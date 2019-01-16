Finagle mysql-util
=============

[![Build Status](https://travis-ci.org/whisklabs/mysql-util.svg?branch=master)](https://travis-ci.org/whisklabs/mysql-util)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.whisk/mysql-util-core_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.whisk/mysql-util-core_2.12)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Features

### core

```scala
libraryDependencies += "com.whisk" % "mysql-util-core_2.12" % "0.4.0"
```

First you need to import:
```scala
import com.whisk.finagle.mysql._
```

#### Client
There are two helper methods around [Client](https://twitter.github.io/finagle/docs/com/twitter/finagle/mysql/Client.html), which in our expereience very common and can remove some boilerplate:

```scala
def prepareAndExecute(sql: String, params: Parameter*): Future[Result] = {
    client.prepare(sql).apply(params: _*)
}

def prepareAndQuery[T](sql: String, params: Parameter*)(mapper: Row => T): Future[Seq[T]] = {
    client.prepare(sql).select[T](params: _*)(mapper)
}
```

Examples:

```scala
client.prepareAndExecute("insert into recipes(id, name) values (?, ?)", recipe.id, recipe.name) //returns Future[Result]

client.prepareAndQuery("select name from recipes where id = ?", recipe.id)(row =>
      row.get[String]("name")) // returns Future[Seq[T]]
```
#### Row extraction helpers

There are number of helper methods on rows to extract values:

```scala
  def getOption[T](name: String)(implicit decoder: ValueDecoder[T]): Option[T]
  def getOption[T](index: Int)(implicit decoder: ValueDecoder[T]): Option[T]
  def get[T](name: String)(implicit decoder: ValueDecoder[T]): T
  def get[T](index: Int)(implicit decoder: ValueDecoder[T]): T
  def getOrElse[T](name: String, default: => T)(implicit decoder: ValueDecoder[T]): T
  def getOrElse[T](index: Int, default: => T)(implicit decoder: ValueDecoder[T]): T
```

Value decoders [provided](/mysql-util-core/src/main/scala/com/whisk/finagle/mysql/ValueDecoder.scala) for all primitive types, which underlying driver supports

so you can write
```scala
row.get[Date]("date_column")
```

You can also implement your own decoders based on primitive one.

For example if you store arrays as comma-separated string in Mysql then you can do:
```scala
implicit val stringArrayDecoder: ValueDecoder[Array[String]] =
      ValueDecoder.string.map(_.split(","))

client
  .prepareAndQuery("select tags from recipes where id = ?", id)(row =>
    row.get[Array[String]]("tags"))
```

#### support for MySQL JSON Datatype

In core module there is a support for extracting json datatype into raw string.

It is wrapped into:
```scala
case class RawJsonString(value: String)
```

`ValueDecoder` is provided for it
```scala
row.get[RawJsonString]("data")
```

#### parameters

Finagle Mysql driver out of the box doesn't allow to pass `scala.Option` as parameters. This library does

### Testing

```scala
libraryDependencies += "com.whisk" % "mysql-util-testing_2.12" % "0.1.2"
```

There is a library which you can include for integration testing purposes against real MySQL instance in Docker container.

Library boostrapping Docker container, can optionally preload sql and configures instance of [Client](https://twitter.github.io/finagle/docs/com/twitter/finagle/mysql/Client.html).

Example:
```scala
import com.twitter.finagle.mysql.Client
import com.whisk.finagle.mysql.testing.MysqlTestkit
import org.scalatest.Suite

trait MysqlTestBase extends MysqlTestkit { self: Suite =>

  override val mysqlInitSchemaPaths = Seq("/schema/tables.ddl")

  protected lazy val client: Client = mysqlClient.get()
}
```

### JSON (Circe)

```scala
libraryDependencies += "com.whisk" % "mysql-util-circe_2.12" % "0.1.2"
```

support comes with row extraction helpers. you can use all regular methods from `core` module 
for extracting `io.circe.Json` and `io.circe.JsonObject` 

```scala
import com.whisk.finagle.mysql._
import com.whisk.finagle.mysql.circe._
import io.circe._

row.get[Json]("data")
row.get[JsonObject]("data")
```

you can also extract you own types if there is `io.circe.Decoder[T]` implicit in scope:

```scala
implicit val metadataCirceDecoder: Decoder[RecipeMetadata] =
    Decoder.forProduct2("cuisine", "mealType")(RecipeMetadata.apply)

row.json[RecipeMetadata]("data") mustBe expectedValue
```

all methods:

```scala
def jsonOption[T](name: String)(implicit decoder: Decoder[T]): Option[T]
def jsonOption[T](index: Int)(implicit decoder: Decoder[T]): Option[T]
def json[T](name: String)(implicit decoder: Decoder[T]): T
def json[T](index: Int)(implicit decoder: Decoder[T]): T
def jsonOrElse[T](name: String, default: => T)(implicit decoder: Decoder[T]): T
def jsonOrElse[T](index: Int, default: => T)(implicit decoder: Decoder[T]): T
```
