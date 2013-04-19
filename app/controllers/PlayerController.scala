package controllers

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._
import anorm._

import models._

import play.api.libs.json.Json

object PlayerController extends Controller {

	final val PAGE_SIZE = 10

  val playerForm = Form(
  	mapping(
  		"id" -> ignored(NotAssigned: Pk[Long]),
  		"name" -> nonEmptyText,
  		"email" -> email
  	)((id, name, email) => Player(id, name, Record()))
  	(player => Some(player.id, player.name, "email"))
  )

	def list(page: Int) = Action { implicit request =>
			if (page <= 0 || page > Page.last(Player.count, PAGE_SIZE))
				Redirect(routes.PlayerController.list())	
			else
				Ok(views.html.player.ranking(Player.findByPage(page, PAGE_SIZE)))
	}

	def create = Action { implicit request => 
  	Ok(views.html.player.create(playerForm))
  }

	def save = Action { implicit request =>
		Redirect(routes.PlayerController.list())
	}


	def view(id: Long) = Action { implicit request =>
		Player.findById(id) match {
			case None => NotFound
			case Some(p) => Ok(views.html.player.view(p, Match.findByPlayerPage(id, 1, 3), p.findRivalries))
		}
	}

	/**
	 * JSON autocomplete
	 * @param filter part of the player's name
	 * @return all players matching the filter
	 */
	def names(filter: String) = Action {
		Ok(Json.toJson(
			Player.findByNameLike("%" + filter + "%").map(_.name)
		))
	}
}