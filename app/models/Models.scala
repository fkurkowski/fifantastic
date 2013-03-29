package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import scala.language.postfixOps

case class Player(id: Pk[Long] = NotAssigned, name: String, record: Record)
case class Team(id: Pk[Long] = NotAssigned, name: String)
case class Match(id: Pk[Long] = NotAssigned, home: PlayerScore, away: PlayerScore)
case class PlayerScore(player: Player, team: Team, goals: Int)
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

	def findAll: List[Team] = {
		DB.withConnection { implicit connection =>
			SQL("select * from team")
				.as(Team.parser *)
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

	def create(name: String): Long = {
		DB.withConnection { implicit connection => 
			SQL(
				"""
					insert into team(name) 
					values ({name})
				"""
			).on('name -> name).executeInsert().get
		}
	}
}

object Match {

	val parser = {
		get[Pk[Long]]("match.id") ~
		get[Long]("match.home_player_id") ~
		get[Long]("match.home_team_id") ~
		get[Int]("match.home_goals") ~
		get[Long]("match.away_player_id") ~
		get[Long]("match.away_team_id") ~
		get[Int]("match.away_goals") map {
			case id~hpId~htId~homeGoals~apId~atId~awayGoals =>
				Match(
					id, 
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
			SQL("select * from match").as(Match.parser *)
		}
	}

	def create(hp: Long, ht: Long, hg: Int, ap: Long, 
		at: Long, ag: Int): Long = {
		DB.withConnection { implicit connection => 
			SQL(
				"""
					insert into match(home_player_id, home_team_id,
						home_goals, away_player_id, away_team_id, away_goals)
					values({hp}, {ht}, {hg}, {ap}, {at}, {ag})
				"""
			).on(
				'hp -> hp,
				'ht -> ht,
				'hg -> hg,
				'ap -> ap,
				'at -> at,
				'ag -> ag
			).executeInsert().get
		}
	}
	
}

