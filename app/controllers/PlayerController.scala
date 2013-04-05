package controllers

import play.api._
import play.api.mvc._
import models._

import play.api.libs.json.Json

object PlayerController extends Controller {

	final val PAGE_SIZE = 10

	def names(filter: String) = Action {
		Ok(Json.toJson(
			Player.findByNameLike("%" + filter + "%").map(_.name)
		))
	}

	def ranking(page: Int) = Action { implicit request =>
			if (page <= 0 || page > Page.last(Player.count, PAGE_SIZE))
				Redirect(routes.PlayerController.ranking())
			else
				Ok(views.html.ranking(Player.findByPage(page, PAGE_SIZE)))
	}
}