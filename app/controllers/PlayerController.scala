package controllers

import play.api._
import play.api.mvc._
import models._

import play.api.libs.json.Json

object PlayerController extends Controller {
	def names(filter: String) = Action {
		Ok(Json.toJson(
			Player.findByNameLike("%" + filter + "%").map(_.name)
		))
	}
}