package test

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

class PlayerSpec extends Specification {

	import models._

	"Player model" should {

		"be found by id" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				val Some(player) = Player.findById(1)
				player.name must_== "Bruce Grannec"
			}
		}

		"be found by name" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				val Some(player) = Player.findByName("Bruce Grannec")
				player.name must_== "Bruce Grannec"
			}	
		}

		"be found by name like" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				val players = Player.findByNameLike("%Ovi%")
				players.size must_== 1
			}	
		}

		"find all" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				val all = Player.findAll
				all.size must_== 10
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
				val id = Player.create("Felipe Kurkowski")

				val Some(player) = Player.findById(id)
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
}