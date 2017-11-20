package com.whisk.finagle

package object mysql extends MysqlClientImplicits with RowImplicits with ParameterImplicits {

  implicit class SqlStringContext(stringContext: StringContext) {
    def sql(args: QueryParameter*) = InterpolatedQuery.fromParts(stringContext.parts, args)
  }

  def tuple(parameters: SingleParameter*) = {
    new TupleParameter(parameters)
  }
}
