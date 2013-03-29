package test

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

class ModelSpec extends Specification {

	import models._

		"Player model" should {

			"be found by id" in {
				running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
					val Some(player) = Player.findById(1)
					player.name must_== "Bruce Grannec"
				}
			}

			"find all" in {
				running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
					val all = Player.findAll
					all.size must_== 2
				}
			}

			"be updated" in {
				running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
					Player.update(1, Player(name = "Bruce 'The Machine' Grannec", 
						record = Record(wins = 84, draws = 11, losses = 4, goalsScored = 192, 
							goalsConceded = 68)))

					val Some(player) = Player.findById(1)
					player.name must_== "Bruce 'The Machine' Grannec"
					player.record.wins must_== 84
				}
			}

			"be created" in {
				running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
					val created = Player.create("Felipe Kurkowski")

					val Some(player) = Player.findById(created.id.get)
					player.name must_== "Felipe Kurkowski"
					player.record.wins must_== 0
				}
			}
			
	}

	"Record model" should {

		"be found by id" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				val Some(record) = Record.findById(1)
				record.wins must_== 82
			}
		}

		"be found by player id" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				val Some(record) = Record.findByPlayerId(1)
				record.wins must_== 82
			}
		}

		"be updated" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				Record.update(1, Record(wins = 83, draws = 11, losses = 4,
					goalsScored = 192, goalsConceded = 68))
				
				val Some(record) = Record.findByPlayerId(1)
				record.wins must_== 83
			}
		}
	}

	"Team model" should {

		"be found by id" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				val Some(team) = Team.findById(1)
				team.name must_== "Real Madrid"
			}
		}

		"find all" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				val all = Team.findAll
				all.size must_== 2
			}
		}

		"be updated" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				Team.update(1, Team(name = "Real Madrid Castilla"))

				val Some(team) = Team.findById(1)
				team.name must_== "Real Madrid Castilla"
			}
		}

		"be created" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				val created = Team.create("Barcelona")

				val Some(team) = Team.findById(created.id.get)
				team.name must_== "Barcelona"
			}
		}
	}

	"Match model" should {
		"be found by id" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				val Some(game) = Match.findById(1)
				
				game.home.player.name must_== "Bruce Grannec"
				game.home.team.name must_== "Real Madrid"
				game.home.goals must_== 2
				game.away.player.name must_== "Alfonso Ramos Cuevas"
				game.away.team.name must_== "Manchester City"
				game.away.goals must_== 2
			}
		}

		"be found by player id" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				val games = Match.findByPlayerId(1)
				
				games.size must_== 3
			}
		}

		"be found by team id" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				val games = Match.findByTeamId(1)
				games.size must_== 2
			}
		}

		"find all" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				val games = Match.findAll
				games.size must_== 3
			}
		}
	}
}