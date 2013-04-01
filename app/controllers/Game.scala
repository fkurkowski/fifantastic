package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import java.util.Date

import anorm._
import models._

object Game extends Controller {

	val gameForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "home_player_name" -> nonEmptyText,
      "home_team_name" -> nonEmptyText,
      "home_goals" -> number,
      "away_player_name" -> nonEmptyText,
      "away_team_name" -> nonEmptyText,
      "away_goals" -> number
    )((id, hpn, htn, hg, apn, atn, ag) => Match(id, new Date(), PlayerScore(Player.findByName(hpn).get, Team.findByName(htn).get, hg), 
    		PlayerScore(Player.findByName(apn).get, Team.findByName(atn).get, ag)))
     ((game: Match) => Some(game.id, game.home.player.name, game.home.team.name, game.home.goals,
     		game.away.player.name, game.away.team.name, game.away.goals)))

	val index = Redirect(routes.Game.list)

	def list = Action { implicit request =>
  	Ok(views.html.list(Match.findAll))
  }

  def random = list

  def save = Action { implicit request =>
  	gameForm.bindFromRequest.fold(
  		formWithErrors => BadRequest(views.html.form(formWithErrors, playersAsString, teamsAsString)),
  		game => {
  			Match.create(game)
  			index.flashing("success" -> "Match has been added")
  		}
  	)
  }

  def form = Action {
  	Ok(views.html.form(gameForm, playersAsString, teamsAsString))
  }

  def asJSArray(list: List[String]) = "[\"" + list.mkString("\", \"") + "\"]"
  def playersAsString = asJSArray(Player.findAll.map(_.name))
  def teamsAsString = asJSArray(Team.findAll.map(_.name))
}