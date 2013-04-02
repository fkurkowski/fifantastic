#!/bin/sh
exec scala "$0" "$@"
!#

import java.util._

def format(id: Int, cal: Calendar, hp: Int, ht: Int, hg: Int, 
	ap: Int, at: Int, ag: Int) = 
	"insert into match values (%d, '2013-%d-%d', %d, %d, %d, %d, %d, %d);"
		.format(id, cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), 
			hp, ht, hg, ap, at, ag)

def rand(n: Int): Int = (n.toDouble * Math.random()).toInt + 1
def adjust(c: Calendar) = {
	c.set(Calendar.DAY_OF_YEAR, rand(366))
	c
}
def close(n: Int) = {
	if (n <= 25) rand(25)
	else if (n > 25 && n <= 75) n - 25 + rand(50)
	else 75 + rand(25)
}


def genGames(n: Int) = {
	val cal = Calendar.getInstance()

	for {
  	i <- 1 to n
  	hp = rand(10)
  	ap = rand(10)
  	ht = rand(100)
  	if hp != ap
  } yield format(i, adjust(cal), hp, ht, rand(5) - 1, ap, close(ht), rand(5) - 1)
}

def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
  val p = new java.io.PrintWriter(f)
  try { op(p) } finally { p.close() }
}

printToFile(new java.io.File("inserts.txt"))(p => {
	genGames(1000).foreach(p.println)
})
