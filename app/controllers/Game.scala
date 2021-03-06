package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import anorm._

import java.util.Date
import scala.util.Random

import models._

object Game extends Controller {

  def exists[A](lookup: String => Option[A]) =
    nonEmptyText.verifying("entity.not.found", name => lookup(name) != None)

  val scoreMapping = mapping(
        "player" -> exists(Player.findByName),
        "team" -> exists(Team.findByName),
        "goals" -> number(min=0)
      )((player, team, goals) => PlayerScore(Player.findByName(player).get, Team.findByName(team).get, goals)
      )(score => Some(score.player.name, score.team.name, score.goals))

  val gameForm = Form(
    "game" -> mapping(
      "id" -> ignored(NotAssigned: Pk[Long]),
      "home" -> scoreMapping,
      "away" -> scoreMapping
    )((id, home, away) => Match(id, new Date(), home, away))
    (game => Some(game.id, game.home, game.away))
    .verifying("player.different", game => game.home.player != game.away.player)
  )

  val main = Redirect(routes.Game.list())

	def index = list()

	def list(page: Int = 1) = Action { implicit request =>
  	Ok(views.html.game.list(Match.findByPage(page)))
  }

  def random = Action { implicit request =>
    val matches = Match.findAll.filter { game =>
      (game.home.goals - game.away.goals).abs >= 3
    }

    val game = matches match {
      case Nil => None
      case x => Some(x(Random.nextInt(x.length)))
    }

    Ok(views.html.game.memorable(game))
  }

  def save = Action { implicit request =>
  	gameForm.bindFromRequest.fold(
  		formWithErrors => {
        BadRequest(views.html.game.create(formWithErrors))
      }, 
  		game => {
  			Match.create(game)
  			main.flashing("success" -> "Match has been added")
  		}
  	)
  }

  def create = Action { implicit request => 
  	Ok(views.html.game.create(gameForm))
  }

}