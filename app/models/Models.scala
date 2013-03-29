package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class Player(id: Pk[Long] = NotAssigned, name: String, record: Record)
case class Team(id: Pk[Long] = NotAssigned, name: String)
case class Match(id: Pk[Long] = NotAssigned, home: PlayerScore, away: PlayerScore)
case class PlayerScore(player: Player, team: Team, goals: Int)
case class Record(id: Pk[Long] = NotAssigned, wins: Int, draws: Int, losses: Int, 
	goalsScored: Int, goalsConceded: Int)

object Record {
	val parser = {
		get[Pk[Long]]("record.id") ~
		get[Int]("record.wins") ~
		get[Int]("record.draws") ~
		get[Int]("record.losses") ~
		get[Int]("record.goals_scored") ~
		get[Int]("record.goals_conceded") map {
			case id~wins~draws~losses~goalsScored~goalsConceded =>
				Record(id, wins, draws, losses, goalsScored, goalsConceded)
		}
	}

	def findById(id: Long): Option[Record] = {
		DB.withConnection { implicit connection =>
			SQL("select * from record where id = {id}")
				.on('id -> id)
				.as(Record.parser.singleOpt)
		}
	}

	def update(id: Long, record: Record) = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					update record
					set wins = {wins}, draws = {draws}, losses = {losses}, 
					goals_scored = {goals_scored}, goals_conceded = {goals_conceded}
					where id = {id}
				"""
			).on(
				'id -> id, 
				'wins -> record.wins,
				'draws -> record.draws,
				'losses -> record.losses,
				'goals_scored -> record.goalsScored,
				'goals_conceded -> record.goalsConceded
			).executeUpdate()
		}
	}
}

