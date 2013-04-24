package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import scala.language.postfixOps

case class Player(id: Pk[Long] = NotAssigned, name: String, record: Record = Record()) {
	/**
	 * Every player has a serie of rivalries with other players. Each
	 * of these rivalries has it's own name and specific properties. For
	 * example, a 'nemesis' rivalry is determined by the player who has
	 * the biggest (wins-losses) against this player.
	 *
   * @return A list containing all rivalries
	 */
	def findRivalries(): List[Rivalry] = {

		val matches = Match.findByPlayerId(id.get)

		val byRecord = matches.foldLeft(Map[Player, Record]()) { (map, game) =>
			
			val opp = if (game.home.player == this) game.away.player
								else game.home.player
			
			val current = map.getOrElse(opp, Record())

			map + (opp -> (current + game.outcome(this)))
		}.toList sortWith { (e1, e2) =>
			(e1._2.wins - e1._2.losses > e2._2.wins - e2._2.losses)
		}

		byRecord match {
			case head :: Nil => List()

			case head :: tail => List(
				Rivalry("Nemesis", tail.last._1, tail.last._2),
				Rivalry("Bonus", head._1, head._2)
			) 

			case _ => List()
		}
		

		/*val nemesis = byRecord.last
    val bonus = byRecord(0)

    List(
    	Rivalry("Nemesis", nemesis._1, nemesis._2),
    	Rivalry("Bonus", bonus._1, bonus._2)
    )*/
	}

	def + (outcome: Outcome) = Player(id, name, record + outcome)
}

case class Record(wins: Int = 0, draws: Int = 0, losses: Int = 0, 
	goalsScored: Int = 0, goalsConceded: Int = 0) {

	def total = wins + draws + losses

	def percent = "%.1f".format {
			if (total > 0) ((wins*3 + draws).toDouble / (total*3)) * 100
			else 0f
	}

	def + (outcome: Outcome) = outcome.result match {
		case Result.Win => Record(wins + 1, draws, losses, 
			goalsScored + outcome.scored, goalsConceded + outcome.conceded)
		case Result.Draw => Record(wins, draws + 1, losses, 
			goalsScored + outcome.scored, goalsConceded + outcome.conceded)
		case Result.Loss => Record(wins, draws, losses + 1, 
			goalsScored + outcome.scored, goalsConceded + outcome.conceded)
	}
}

case class Rivalry(name: String, rival: Player, record: Record)

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
					where player.id = {id}
				"""
			).on('id -> id).as(Player.withRecord.singleOpt)
		}	
	}

	def findByName(name: String): Option[Player] = {
		DB.withConnection { implicit connection => 
			SQL(
				"""
					select * from player
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
					where name like {filter}
				"""
			).on('filter -> filter).as(Player.withRecord *)
		}
	}

	def findByPage(page: Int = 1, pageSize: Int = 10): Page[Player] = {

		val offset = pageSize * (page - 1)

		DB.withConnection { implicit connection =>
			val players = SQL(
				"""
					select *, ISNULL(
						((wins*3+draws)/NULLIF(((wins+draws+losses)*3.0), 0)),
						0
					) as percent
					from player
					order by percent desc, wins desc, (goals_scored - goals_conceded) desc, 
									 name
					limit {pageSize} offset {offset}
				"""
			).on(
				'pageSize -> pageSize,
				'offset -> offset
			).as(Player.withRecord *)

			Page(players, page, pageSize, offset, Player.count)
		}
	}

	def findAll: List[Player] = {
		DB.withConnection { implicit connection => 
			SQL(
				"""
					select * from player
				"""
			).as(Player.withRecord *)
		}
	}

	def count(): Long = {
		DB.withConnection { implicit connection =>
			val total = SQL(
					"""
						select count(*) from player
					"""
				).as(scalar[Long].single)

			total
		}
	}

	def update(id: Long, player: Player) = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					update player
					set name = {name}, wins = {wins}, draws = {draws}, losses = {losses}, 
						goals_scored = {goalsScored}, goals_conceded = {goalsConceded}
					where id = {id}
				"""
			).on(
				'id -> id, 
				'name -> player.name,
				'wins -> player.record.wins,
				'draws -> player.record.draws,
				'losses -> player.record.losses,
				'goalsScored -> player.record.goalsScored,
				'goalsConceded -> player.record.goalsConceded
			).executeUpdate()
		}
	}

	def create(player: Player): Long = {
		DB.withConnection { implicit connection => 
			SQL(
				"""
					insert into player(name, wins, draws, losses, 
						goals_scored, goals_conceded) 
					values ({name}, {wins}, {draws}, {losses}, 
						{goalsScored}, {goalsConceded})
				"""
			).on(
				'name -> player.name,
				'wins -> player.record.wins,
				'draws -> player.record.draws,
				'losses -> player.record.losses,
				'goalsScored -> player.record.goalsScored,
				'goalsConceded -> player.record.goalsConceded
			).executeInsert().get
		}
	}
}

object Record {
	val parser = {
		get[Int]("player.wins") ~
		get[Int]("player.draws") ~
		get[Int]("player.losses") ~
		get[Int]("player.goals_scored") ~
		get[Int]("player.goals_conceded") map {
			case wins~draws~losses~goalsScored~goalsConceded =>
				Record(wins, draws, losses, goalsScored, goalsConceded)
		}
	}
}