package test

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import java.util.Date

class MatchSpec extends Specification {

	import models._

	"Match model" should {
		"be found by id" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				val Some(game) = Match.findById(1)
				
				game.home.player.name must_== "Bruce Grannec"
				game.home.team.name must startWith("Real Madrid")
				game.home.goals must_== 2
				game.away.player.name must_== "Alfonso Ramos"
				game.away.team.name must startWith("FC Barcelona")
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

		"be created" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				val id = Match.create(new Date(), 2, 2, 0, 1, 2, 0)

				val Some(game) = Match.findById(id)
				game.home.player.name must_== "Alfonso Ramos"
				game.home.team.name must startWith("FC Barcelona")
				game.home.goals must_== 0
				game.away.player.name must_== "Bruce Grannec"
				game.away.team.name must startWith("FC Barcelona")
				game.away.goals must_== 0
			}
		}
	}
}