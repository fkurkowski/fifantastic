package test

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

class ModelSpec extends Specification {

	import models._

	"Record model" should {

		"be found by id" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				val Some(record) = Record.findById(1)
				record.wins must_== 82
			}
		}

		"be updated" in {
			running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
				Record.update(1, Record(wins = 83, draws = 11, losses = 4,
					goalsScored = 192, goalsConceded = 68))
				
				val Some(record) = Record.findById(1)
				record.wins must_== 83
			}
		}
	}
}