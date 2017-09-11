package com.whisk.finagle.mysql.test

object Model {

  case class Recipe(id: String, name: String)

  case class RecipeMetadata(cuisine: Option[String] = None, mealType: Option[String] = None)
}
