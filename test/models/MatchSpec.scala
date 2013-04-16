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
				
				game.home.player.name must_== "Ovidiu Patrascu"
				game.home.team.name must startWith("Paris Saint-Germain")
				game.home.goals must_== 0
				game.away.player.name must_== "Marc Arisa"
				game.away.team.name must startWith("Ajax")
				game.away.goals must_== 2
			}
		}

		"be found by player id" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				val games = Match.findByPlayerId(1)
				
				games.size must_== 198
			}
		}

		"be found by team id" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				val games = Match.findByTeamId(1)
				games.size must_== 18
			}
		}

		"be found by page" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				val games = Match.findByPage(2, 1)
				games.prev must beSome.which(_ == 1)
				games.next must beSome.which(_ == 3)
			}
		}

		"be found by player page" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				val id: Long = 1
				val page = Match.findByPlayerPage(id, 2, 1)
				page.items must haveAllElementsLike { 
					case m => {
						List(m.home.player.id.get, m.away.player.id.get) must haveOneElementLike {
							case i => i must_== id
						}
					}
				}
			}
		}

		"find all" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				val games = Match.findAll
				games.size must_== 906
			}
		}

		"be created" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				val id = Match.create(Match(date = new Date(), 
					home = PlayerScore(Player.findById(2).get, Team.findById(2).get, 0),
					away = PlayerScore(Player.findById(1).get, Team.findById(2).get, 0)))

				val Some(game) = Match.findById(id)
				game.home.player.name must_== "Alfonso Ramos"
				game.home.team.name must startWith("FC Barcelona")
				game.home.goals must_== 0
				game.away.player.name must_== "Bruce Grannec"
				game.away.team.name must startWith("FC Barcelona")
				game.away.goals must_== 0
			}
		}

		"update record after creating" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {

				val home = Player.findById(1).get
				val away = Player.findById(2).get

				Match.create(Match(date = new Date(),
					home = PlayerScore(home, Team.findById(1).get, 1),
					away = PlayerScore(away, Team.findById(1).get, 0)))

				val nHome = Player.findById(1).get
				val nAway = Player.findById(2).get

				home.record.wins must_== nHome.record.wins - 1
				away.record.losses must_== nAway.record.losses - 1
			}
		}
	}
}