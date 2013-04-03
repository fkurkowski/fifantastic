package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import scala.language.postfixOps


case class Team(id: Pk[Long] = NotAssigned, name: String)

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

	def findByName(name: String): Option[Team] = {
		DB.withConnection { implicit connection => 
			SQL(
				"""
					select * from team
					where name = {name}
				"""
			).on('name -> name).as(Team.parser.singleOpt)
		}
	}

	def findByNameLike(filter: String): List[Team] = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					select * from team
					where name like {filter}
				"""
			).on('filter -> filter).as(Team.parser *)
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