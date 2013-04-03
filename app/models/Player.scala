package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import scala.language.postfixOps

case class Player(id: Pk[Long] = NotAssigned, name: String, record: Record)
case class Record(id: Pk[Long] = NotAssigned, wins: Int, draws: Int, losses: Int, 
	goalsScored: Int, goalsConceded: Int)

object Player {
	val withRecord = {
		get[Pk[Long]]("player.id") ~
		get[String]("player.name") ~
		Record.parser map {
			case id~name~record => Player(id, name, record)
		}
	}

	def findById(id: Long): Option[Player] = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					select * from player 
					left join record on player.id = record.player_id 
					where player.id = {id}
				"""
			).on('id -> id).as(Player.withRecord.singleOpt)
		}	
	}

	def findAll: List[Player] = {
		DB.withConnection { implicit connection => 
			SQL(
				"""
					select * from player
					left join record on player.id = record.player_id
				"""
			).as(Player.withRecord *)
		}
	}

	def findByName(name: String): Option[Player] = {
		DB.withConnection { implicit connection => 
			SQL(
				"""
					select * from player
					left join record on player.id = record.player_id
					where name = {name}
				"""
			).on('name -> name).as(Player.withRecord.singleOpt)
		}
	}

	def findByNameLike(filter: String): List[Player] = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					select * from player
					left join record on player.id = record.player_id
					where name like {filter}
				"""
			).on('filter -> filter).as(Player.withRecord *)
		}
	}

	def update(id: Long, player: Player) = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					update player
					set name = {name}
					where id = {id}
				"""
			).on(
				'id -> id, 
				'name -> player.name
			).executeUpdate()
		}

		Record.update(id, player.record)
	}

	def create(name: String): Long = {
		val id = DB.withConnection { implicit connection => 
			SQL(
				"""
					insert into player(name) 
					values ({name})
				"""
			).on('name -> name).executeInsert().get
		}

		Record.create(id)
		id
	}
}

/** 
 * TODO: Apparently, there's no need to keep this in a separate table.
 */
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

	def findByPlayerId(playerId: Long): Option[Record] = {
		DB.withConnection { implicit connection => 
			SQL("select * from record where player_id = {player_id}")
				.on('player_id -> playerId)
				.as(Record.parser.singleOpt)
		}
	}

	def update(playerId: Long, record: Record) = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					update record
					set wins = {wins}, draws = {draws}, losses = {losses}, 
					goals_scored = {goals_scored}, goals_conceded = {goals_conceded}
					where player_id = {player_id}
				"""
			).on(
				'player_id -> playerId, 
				'wins -> record.wins,
				'draws -> record.draws,
				'losses -> record.losses,
				'goals_scored -> record.goalsScored,
				'goals_conceded -> record.goalsConceded
			).executeUpdate()
		}
	}

	def create(playerId: Long): Long = {
		DB.withConnection { implicit connection => 
			SQL(
				"""
					insert into record(wins, draws, losses, goals_scored, 
						goals_conceded, player_id) 
					values (0, 0, 0, 0, 0, {player_id})
				"""
			).on('player_id -> playerId).executeInsert().get
		}
	}
}