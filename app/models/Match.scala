package models

import play.api.db._
import play.api.Play.current
import java.util.Date

import anorm._
import anorm.SqlParser._

import scala.language.postfixOps

case class Match(id: Pk[Long] = NotAssigned, date: Date, home: PlayerScore, away: PlayerScore) {
	def winner: Option[PlayerScore] = {
		if (home.goals > away.goals) Some(home)
		else if (home.goals < away.goals) Some(away)
		else None
	}

	def loser: Option[PlayerScore] = winner match {
		case Some(ps) if ps == home => Some(away)
		case Some(ps) => Some(home)
		case None => None
	}

	def outcome(player: Player) = (winner, loser) match {
		case (Some(w), Some(l)) if w.player == player => Outcome(Result.Win, w.goals, l.goals)
		case (Some(w), Some(l)) => Outcome(Result.Loss, l.goals, w.goals)
		case _ => Outcome(Result.Draw, home.goals, home.goals)
	}
}

case class PlayerScore(player: Player, team: Team, goals: Int)

case class Outcome(result: Result.Value, scored: Int, conceded: Int)
object Result extends Enumeration {
    type Result = Value
    val Win, Draw, Loss = Value
 }

object Match {

	val parser = {
		get[Pk[Long]]("match.id") ~
		get[Date]("match.when") ~
		get[Long]("match.home_player_id") ~
		get[Long]("match.home_team_id") ~
		get[Int]("match.home_goals") ~
		get[Long]("match.away_player_id") ~
		get[Long]("match.away_team_id") ~
		get[Int]("match.away_goals") map {
			case id~date~hpId~htId~homeGoals~apId~atId~awayGoals =>
				Match(
					id, date,
					PlayerScore(Player.findById(hpId).get, Team.findById(htId).get, homeGoals),
					PlayerScore(Player.findById(apId).get, Team.findById(atId).get, awayGoals)
				)
		}
	}

	def findById(id: Long): Option[Match] = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					select * from match
					where id = {id}
				"""
			).on('id -> id).as(Match.parser.singleOpt)
		}
	}

	def findByPlayerId(id: Long): List[Match] = {
		DB.withConnection { implicit connection => 
			SQL(
				"""
					select * from match
					where home_player_id = {id} OR away_player_id = {id}
				"""
			).on('id -> id).as(Match.parser *)
		}
	}

	def findByTeamId(id: Long): List[Match] = {
		DB.withConnection { implicit connection => 
			SQL(
				"""
					select * from match
					where (home_team_id <> away_team_id)
						AND (home_team_id = {id} OR away_team_id = {id})
				"""
			).on('id -> id).as(Match.parser *)
		}
	}

	def findAll: List[Match] = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					select * from match
					order by when desc
				"""
			).as(Match.parser *)
		}
	}

	def findByPage(page: Int = 1, pageSize: Int = 5): Page[Match] = {

		val offset = pageSize * (page - 1)

		DB.withConnection { implicit connection =>
			val games = SQL(
				"""
					select * from match
					order by when desc
					limit {pageSize} offset {offset}
				"""
			).on(
				'pageSize -> pageSize,
				'offset -> offset
			).as(Match.parser *)

			val total = SQL(
				"""
					select count(*) from match
				"""
			).as(scalar[Long].single)

			Page(games, page, pageSize, offset, total)
		}
	}

	def findByPlayerPage(id: Long, page: Int = 1, pageSize: Int = 5): Page[Match] = {

		val offset = pageSize * (page - 1)

		DB.withConnection { implicit connection =>

			val games = SQL(
				"""
					select * from match
					where home_player_id = {id} OR away_player_id = {id}
					order by when desc
					limit {pageSize} offset {offset}
				"""
			).on(
				'id -> id,
				'pageSize -> pageSize,
				'offset -> offset
			).as(Match.parser *)

			val total = SQL(
				"""
					select count(*) from match
					where home_player_id = {id} OR away_player_id = {id}
				"""
			).on(
				'id -> id
			).as(scalar[Long].single)

			Page(games, page, pageSize, offset, total)
		}
	}

	def create(game: Match): Long = {
		DB.withConnection { implicit connection => 

			val id = SQL(
				"""
					insert into match(when, home_player_id, home_team_id,
						home_goals, away_player_id, away_team_id, away_goals)
					values({date}, {hp}, {ht}, {hg}, {ap}, {at}, {ag})
				"""
			).on(
				'date -> game.date,
				'hp -> game.home.player.id.get,
				'ht -> game.home.team.id.get,
				'hg -> game.home.goals,
				'ap -> game.away.player.id.get,
				'at -> game.away.team.id.get,
				'ag -> game.away.goals
			).executeInsert().get

			// Update player records
			Player.update(game.home.player.id.get, game.home.player + game.outcome(game.home.player))
			Player.update(game.away.player.id.get, game.away.player + game.outcome(game.away.player))

			// Return game id
			id
		}
	}
}