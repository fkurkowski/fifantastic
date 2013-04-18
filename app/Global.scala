import play.api._
import models._

import java.util.Calendar
import scala.util.Random

object Global extends GlobalSettings {  
  override def onStart(app: Application) {
    if (app.mode != Mode.Prod) {
    	TestData.insert()
    } 
  }
}

object TestData {
	
	def insert() {
		if (Player.findAll.isEmpty) {

			// Players
			Seq(
				Player(name = "Bruce Grannec"),
				Player(name = "Alfonso Ramos"),
				Player(name = "Ovidiu Patrascu"),
				Player(name = "George Tsirita"),
				Player(name = "Adam Winster"),
				Player(name = "Ty Walton"),
				Player(name = "Oscar Martin"),
				Player(name = "Krasimir Ivanov"),
				Player(name = "Marc Arisa"),
				Player(name = "Daniel Rodriguez")
			).foreach(Player.create)

			// Randomize results
			val teams = Team.findAll

			Seq(
				create(2, 1, 1, 1, 1, 1),
				create(5, 1, 3, 2, 2, 0),
				create(1, 1, 5, 3, 10, 1),
				create(4, 7, 2, 5, 6, 2),
				create(5, 4, 1, 4, 3, 4),
				create(6, 9, 0, 8, 2, 2),
				create(7, 6, 0, 1, 4, 3),
				create(5, 4, 3, 1, 7, 1),
				create(1, 5, 1, 7, 3, 4),
				create(2, 4, 1, 4, 1, 0),
				create(3, 6, 4, 1, 3, 1),
				create(4, 1, 2, 2, 3, 0),
				create(5, 7, 1, 7, 9, 2),
				create(6, 11, 0, 4, 5, 2),
				create(7, 13, 0, 9, 8, 0),
				create(8, 5, 1, 3, 4, 1),
				create(6, 31, 2, 2, 10, 3),
				create(1, 9, 3, 5, 9, 1),
				create(2, 8, 4, 5, 7, 4),
				create(3, 2, 7, 5, 5, 1),
				create(4, 3, 1, 3, 3, 3),
				create(5, 9, 2, 2, 2, 0),
				create(6, 1, 0, 1, 1, 0),
				create(7, 18, 0, 2, 3, 1),
				create(8, 13, 3, 4, 16, 4)
			).foreach(_())
		}
	}

	def create(hp: Int, ht: Int, hg: Int, ap: Int, at: Int, ag: Int) =
		() => Match.create(Match(
				date = date,
				home = PlayerScore(Player.findById(hp).get, Team.findById(ht).get, hg),
				away = PlayerScore(Player.findById(ap).get, Team.findById(at).get, ag))
			)

	def rand(n: Int): Int = (n.toDouble * Math.random()).toInt + 1
	
	def date() = {
		val cal = Calendar.getInstance()
		cal.set(Calendar.DAY_OF_YEAR, rand(366))
		cal.getTime
	}
}

