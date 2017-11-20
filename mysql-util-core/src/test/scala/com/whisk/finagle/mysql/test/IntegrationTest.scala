package com.whisk.finagle.mysql.test

import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.util.TimeZone

import com.twitter.finagle.mysql.{OK, Result, TimestampValue}
import com.twitter.util.{Await, Duration, Future}
import com.whisk.finagle.mysql._
import com.whisk.finagle.mysql.testing.MysqlTestkit
import org.scalatest._

class IntegrationTest extends FunSuite with MysqlTestkit with MustMatchers {

  implicit class RichFuture[T](f: Future[T]) {
    def futureValue: T = Await.result(f, Duration.fromSeconds(5))
  }

  private lazy val client = mysqlClient.get()

  val testTable = "test.test_table"

  private implicit val TsValue: TimestampValue =
    new TimestampValue(TimeZone.getTimeZone("UTC"), TimeZone.getTimeZone("UTC"))

  def cleanDb(): Unit = {
    val dropResult = client.query("DROP TABLE IF EXISTS test.test_table").futureValue

    val createTableResult = client.query("""
                                                   |CREATE TABLE test.test_table (
                                                   | id SERIAL PRIMARY KEY,
                                                   | str_field VARCHAR(40),
                                                   | int_field INT,
                                                   | double_field DOUBLE PRECISION,
                                                   | timestamp_field TIMESTAMP,
                                                   | bool_field BOOLEAN
                                                   |)
                                                 """.stripMargin).futureValue
  }

  def insertSampleData(): Unit = {
    val insertDataQuery =
      client.query(
        """
          |INSERT INTO test.test_table(str_field, int_field, double_field, timestamp_field, bool_field) VALUES
          | ('hello', 1234, 10.5, '2015-01-08 11:55:12', TRUE),
          | ('hello', 5557, -4.51, '2015-01-08 12:55:12', TRUE),
          | ('hello', 7787, -42.51, '2013-12-24 07:01:00', FALSE),
          | ('hello', null, null, '2015-02-24 07:01:00', null),
          | ('goodbye', 4567, 15.8, '2015-01-09 16:55:12', FALSE)
        """.stripMargin)

    val response: Result = insertDataQuery.futureValue

    response.asInstanceOf[OK].affectedRows mustEqual 5
  }

  test("insert and select rows") {
    cleanDb()
    insertSampleData()

    val resultRows = client
      .select(
        "SELECT * FROM test.test_table WHERE str_field='hello' ORDER BY timestamp_field"
      )(identity)
      .futureValue

    resultRows.size mustEqual 4

    val firstRow = resultRows.head

    firstRow.getOption[String]("str_field") must equal(Some("hello"))
    firstRow.get[Int]("int_field") must equal(7787)
    firstRow.getOption[Double]("double_field") must equal(Some(-42.51))
    firstRow.get[Instant]("timestamp_field") must equal(
      LocalDateTime.of(2013, 12, 24, 7, 1).toInstant(ZoneOffset.UTC)
    )
    firstRow.getOption[Boolean]("bool_field") must equal(Some(false))

    // handle nullable values
    val lastRow = resultRows(3)
    lastRow.getOption[Int]("int_field") mustBe 'empty
    lastRow.getOption[Double]("double_field") mustBe 'empty
    lastRow.getOption[Boolean]("bool_field") mustBe 'empty
  }

  test("insert and select rows by field") {
    cleanDb()

    val key = "string_key"

    val resultRows = client
      .execute(
        sql"""INSERT INTO test.test_table(str_field, int_field, double_field, timestamp_field, bool_field)
              VALUES ($key, 9012, 15.8, '2015-01-09 16:55:12', FALSE)""")
      .futureValue

    val selectedByName = client
      .fetch(sql"SELECT * FROM test.test_table WHERE str_field=$key")(identity)
      .futureValue

    selectedByName.size mustEqual 1
  }

  test("update row") {
    cleanDb()
    insertSampleData()

    val updateQueryResponse = client
      .query(
        "UPDATE test.test_table SET str_field='hello_updated' where int_field=4567"
      )
      .futureValue
      .asInstanceOf[OK]

    updateQueryResponse.affectedRows must equal(1)

    val resultRows = client
      .select(
        "SELECT * FROM test.test_table WHERE str_field='hello_updated'"
      )(identity)
      .futureValue

    resultRows.size must equal(1)
    resultRows.head.getOption[String]("str_field") must equal(Some("hello_updated"))
  }

  test("delete rows") {
    cleanDb()
    insertSampleData()

    val response = client
      .query(
        "DELETE FROM test.test_table WHERE str_field='hello'"
      )
      .futureValue
      .asInstanceOf[OK]

    response.affectedRows must equal(4)

    val resultRows = client
      .select(
        "SELECT * FROM test.test_table"
      )(identity)
      .futureValue

    resultRows.size must equal(1)
    resultRows.head.getOption[String]("str_field") must equal(Some("goodbye"))
  }

  test("select rows via prepared query") {
    cleanDb()
    insertSampleData()

    val resultRows = client
      .prepareAndQuery("SELECT * FROM test.test_table WHERE str_field=? AND bool_field=?",
                       "hello",
                       true)(identity)
      .futureValue

    resultRows.size must equal(2)
    resultRows.foreach { row =>
      row.getOption[String]("str_field") must equal(Some("hello"))
      row.getOption[Boolean]("bool_field") must equal(Some(true))
    }
  }

  test("select rows via interpolated query") {
    cleanDb()
    insertSampleData()

    val str = "hello"
    val bool: Option[Boolean] = Some(true)
    val resultRows = client
      .fetch(sql"SELECT * FROM test.test_table WHERE str_field=$str AND bool_field=$bool")(identity)
      .futureValue

    resultRows.size must equal(2)
    resultRows.foreach { row =>
      row.getOption[String]("str_field") must equal(Some("hello"))
      row.getOption[Boolean]("bool_field") must equal(Some(true))
    }
  }

  test("select rows via interpolated query (Seq)") {
    cleanDb()
    insertSampleData()

    val str = "hello"
    val bool = true
    val ints = Set(5557, 0, 7787)
    val resultRows = client
      .fetch(sql"SELECT * FROM test.test_table WHERE int_field IN ($ints)")(identity)
      .futureValue

    resultRows.size must equal(2)
    resultRows.foreach { row =>
      row.getOption[String]("str_field") must equal(Some("hello"))
      ints must contain(row.get[Int]("int_field"))
    }
  }

  test("execute an update via a prepared statement") {
    cleanDb()
    insertSampleData()

    val numRows = client
      .prepareAndExecute(
        "UPDATE test.test_table SET str_field = ?, timestamp_field = ? where int_field = 4567",
        "hello_updated",
        Instant.now()
      )
      .futureValue
      .asInstanceOf[OK]
      .affectedRows

    val resultRows = client
      .select(
        "SELECT * from test.test_table WHERE str_field = 'hello_updated' AND int_field = 4567"
      )(identity)
      .futureValue

    resultRows.size must equal(numRows)
  }

  test("execute interpolated INSERT INTO with batch") {
    cleanDb()
    val tuples = Seq(
      tuple("hello", 1234, 10.5, true),
      tuple("hello", 5557, -4.51, true),
      tuple("hello", 7787, -42.51, false),
      tuple("hello", null, null, null),
      tuple("goodbye", 4567, 15.8, false)
    )
    val result: Result = client
      .execute(
        sql"INSERT INTO test.test_table(str_field, int_field, double_field, bool_field) VALUES $tuples")
      .futureValue
    result.asInstanceOf[OK].affectedRows mustEqual 5

    val resultRows = client
      .select(
        "SELECT * FROM test.test_table WHERE str_field='hello'"
      )(identity)
      .futureValue

    resultRows.size mustEqual 4
  }

//  test("execute an update via a prepared statement using a Some(value)") {
//    cleanDb()
//    insertSampleData()
//
//    val numRows = client
//      .prepareAndExecute(
//        "UPDATE test.test_table SET str_field = ? where int_field = 4567",
//        Some("hello_updated_some")
//      )
//      .futureValue
//
//    val resultRows = client
//      .select(
//        "SELECT * from test.test_table WHERE str_field = 'hello_updated_some' AND int_field = 4567".format(
//          testTable)
//      )(identity)
//      .futureValue
//
//    resultRows.size must equal(numRows)
//  }
//
//  test("execute an update via a prepared statement using a None") {
//    cleanDb()
//    insertSampleData()
//
//    val numRows = client
//      .prepareAndExecute(
//        "UPDATE test.test_table SET str_field = ? where int_field = 4567",
//        None: Option[String]
//      )
//      .futureValue
//
//    val resultRows = client
//      .select(
//        "SELECT * from test.test_table WHERE str_field IS NULL AND int_field = 4567"
//      )(identity)
//      .futureValue
//
//    resultRows.size must equal(numRows)
//  }
//
//  test("return rows from UPDATE...RETURNING") {
//    cleanDb()
//    insertSampleData()
//
//    val resultRows = client
//      .prepareAndQuery(
//        "UPDATE test.test_table SET str_field = ? where int_field = 4567 RETURNING *",
//        "hello_updated"
//      )(identity)
//      .futureValue
//
//    resultRows.size must equal(1)
//    resultRows.head.get[String]("str_field") must equal("hello_updated")
//  }
//
//  test("return rows from DELETE...RETURNING") {
//    cleanDb()
//    insertSampleData()
//
//    client
//      .prepareAndExecute(
//        s"""INSERT INTO test.test_table(str_field, int_field, double_field, timestamp_field, bool_field)
//              VALUES ('delete', 9012, 15.8, '2015-01-09 16:55:12+05:00', FALSE)"""
//      )
//      .futureValue
//
//    val resultRows = client
//      .prepareAndQuery(
//        "DELETE FROM test.test_table where int_field = 9012 RETURNING *"
//      )(identity)
//      .futureValue
//
//    resultRows.size must equal(1)
//    resultRows.head.get[String]("str_field") must equal("delete")
//  }
//
//  test("execute an UPDATE...RETURNING that updates nothing") {
//    cleanDb()
//    insertSampleData()
//    val resultRows = client
//      .prepareAndQuery(
//        "UPDATE test.test_table SET str_field = ? where str_field = ? RETURNING *",
//        "hello_updated",
//        "xxxx"
//      )(identity)
//      .futureValue
//
//    resultRows.size must equal(0)
//  }
//
//  test("execute a DELETE...RETURNING that deletes nothing") {
//
//    cleanDb()
//    insertSampleData()
//
//    val resultRows = client
//      .prepareAndQuery(
//        "DELETE FROM test.test_table WHERE str_field=?",
//        "xxxx"
//      )(identity)
//      .futureValue
//
//    resultRows.size must equal(0)
//  }
//
//  test("support UUID data type") {
//    client
//      .query(
//        """
//          |CREATE TABLE test.uuid_table(id UUID PRIMARY KEY DEFAULT gen_random_uuid(), name STRING);
//        """.stripMargin)
//      .futureValue
//
//    val resultRows = client
//      .prepareAndQuery("INSERT INTO test.uuid_table(name) VALUES ('a'), ('b'), ('c') RETURNING *")(
//        identity)
//      .futureValue
//
//    resultRows.size must equal(3)
//
//    val uuid = UUID.randomUUID()
//    client
//      .prepareAndExecute("INSERT INTO test.uuid_table(id, name) VALUES (?, ?)", uuid, "test_name")
//      .futureValue
//
//    val fetched = client
//      .prepareAndQuery("select * from test.uuid_table where id = ?", uuid)(identity)
//      .futureValue
//    fetched.size must equal(1)
//    fetched.head.get[UUID]("id") mustEqual uuid
//    fetched.head.get[String]("name") mustEqual "test_name"
//  }
//
//  test("support multi-statement DDL") {
//    client.query("""
//                   |CREATE TABLE test.multi_one(id integer);
//                   |CREATE TABLE test.multi_two(id integer);
//                   |DROP TABLE test.multi_one;
//                   |DROP TABLE test.multi_two;
//                 """.stripMargin).futureValue
//  }
}
