package com.whisk.finagle.mysql.test

import java.sql.Timestamp
import java.util.TimeZone

import com.twitter.finagle.mysql._
import com.twitter.finagle.mysql.transport.MysqlBuf
import com.twitter.io.Buf
import com.whisk.finagle.mysql._
import org.scalatest._

case class SingleValueRow(value: Value) extends Row {
  override val fields = Vector()
  override val values = Vector(value)

  override def indexOf(columnName: String): Option[Int] = columnName match {
    case "v" => Some(0)
    case _   => None
  }
}

class RowDecoderTest extends FunSuite {

  test("decode row value") {

    // String
    val strRow = SingleValueRow(StringValue("str"))

    assert(strRow.get[String]("v") == "str")
    assert(strRow.get[String](0) == "str")

    assert(strRow.getOption[String]("a").isEmpty)

    // Timestamp
    implicit val timestampValueLocal: TimestampValue =
      new TimestampValue(TimeZone.getDefault, TimeZone.getDefault)

    val ts = Timestamp.valueOf("2014-10-09 08:27:53.123456")
    val timestampRow =
      SingleValueRow(timestampValueLocal(ts))

    assert(timestampRow.get[Timestamp]("v") == ts)
  }

  test("extracting timestamps from raw") {
    implicit val timestampValueLocal: TimestampValue =
      new TimestampValue(TimeZone.getDefault, TimeZone.getDefault)

    val timestampBinary: Array[Byte] = {
      val bw = MysqlBuf.writer(new Array[Byte](11))
      bw.writeShortLE(2015)
        .writeByte(1)
        .writeByte(2)
        .writeByte(3)
        .writeByte(4)
        .writeByte(5)
        .writeIntLE(678901)

      Buf.ByteArray.Owned.extract(bw.owned())
    }
    val raw = RawValue(Type.Timestamp, MysqlCharset.Binary, true, timestampBinary)
    val row = SingleValueRow(raw)

    assert(row.get[Timestamp]("v") == Timestamp.valueOf("2015-01-02 03:04:05.678901"))
  }

}
