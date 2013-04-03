package controllers

import play.api._
import play.api.mvc._
import models._

import play.api.libs.json.Json

object TeamController extends Controller {
	def names(filter: String) = Action {
		Ok(Json.toJson(
			Team.findByNameLike("%" + filter + "%").map(_.name)
		))
	}
}