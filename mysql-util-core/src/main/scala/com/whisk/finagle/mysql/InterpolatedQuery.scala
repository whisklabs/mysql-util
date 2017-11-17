package com.whisk.finagle.mysql

class InterpolatedQuery(val preparedQuery: String, val params: Seq[QueryParameter]) {

  def +(query: InterpolatedQuery) =
    new InterpolatedQuery(preparedQuery + query.preparedQuery, params ++ query.params)
}

object InterpolatedQuery {

  def fromParts(parts: Seq[String], params: Seq[QueryParameter]): InterpolatedQuery = {
    val stringBuilder = new StringBuilder()
    parts.zip(params).foreach {
      case (part, param) =>
        stringBuilder ++= part
        param.appendPlaceholders(stringBuilder)
    }
    stringBuilder ++= parts.last
    new InterpolatedQuery(stringBuilder.toString(), params)
  }

}
