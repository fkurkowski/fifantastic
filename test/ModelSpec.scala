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

		"be updated" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				Team.update(1, Team(name = "Real Madrid Castilla"))

				val Some(team) = Team.findById(1)
				team.name must_== "Real Madrid Castilla"
			}
		}
	}
}