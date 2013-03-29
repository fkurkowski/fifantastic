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

object Player {
	val parser = {
		get[Pk[Long]]("player.id") ~
		get[String]("player.name") map {
			case id~name => Player(id, name, Record.findByPlayerId(id.get).get)
		}
	}

	def findById(id: Long): Option[Player] = {
		DB.withConnection { implicit connection =>
			SQL("select * from player where id = {id}")
				.on('id -> id)
				.as(Player.parser.singleOpt)
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

	def create(name: String): Player = {
		val id = DB.withConnection { implicit connection => 
			SQL(
				"""
					insert into player(name) 
					values ({name})
				"""
			).on('name -> name).executeInsert().get
		}

		Player(Id(id), name, Record.create(id))
	}
}

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

	def create(playerId: Long): Record = {
		val id = DB.withConnection { implicit connection => 
			SQL(
				"""
					insert into record(wins, draws, losses, goals_scored, 
						goals_conceded, player_id) 
					values (0, 0, 0, 0, 0, {player_id})
				"""
			).on('player_id -> playerId).executeInsert().get
		}

		Record(Id(id), 0, 0, 0, 0, 0)
	}
}

object Team {
	val parser = {
		get[Pk[Long]]("team.id") ~
		get[String]("team.name") map {
			case id~name => Team(id, name)
		}
	}

	def findById(id: Long): Option[Team] = {
		DB.withConnection { implicit connection =>
			SQL("select * from team where id = {id}")
				.on('id -> id)
				.as(Team.parser.singleOpt)
		}	
	}

	def update(id: Long, team: Team) = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					update team
					set name = {name}
					where id = {id}
				"""
			).on(
				'id -> id, 
				'name -> team.name
			).executeUpdate()
		}
	}

	def create(name: String): Team = {
		val id = DB.withConnection { implicit connection => 
			SQL(
				"""
					insert into team(name) 
					values ({name})
				"""
			).on('name -> name).executeInsert().get
		}

		Team(Id(id), name)
	}
}

