Finagle mysql-util
=============

[![Build Status](https://travis-ci.org/whisklabs/mysql-util.svg?branch=master)](https://travis-ci.org/whisklabs/mysql-util)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.whisk/mysql-util-core_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.whisk/mysql-util-core_2.12)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Features

### core

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
