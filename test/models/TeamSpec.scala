package test

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

class TeamSpec extends Specification {

	import models._

	"Team model" should {

		"be found by id" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				val Some(team) = Team.findById(1)
				team.name must startWith("Real Madrid")
			}
		}

		"be found by name" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				val Some(team) = Team.findByName("FC Barcelona")
				team.name must_== "FC Barcelona"
			}	
		}

		"be found by name like" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				val teams = Team.findByNameLike("%Barcelona%")
				teams.size must_== 3
			}	
		}

		"find all" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				val all = Team.findAll
				all.size must_== 574
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
				val id = Team.create("Barcelona")

				val Some(team) = Team.findById(id)
				team.name must_== "Barcelona"
			}
		}
	}
}