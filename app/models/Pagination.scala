package models

/**
 * Pagination helper, from Play samples.
 */
case class Page[+A](items: Seq[A], page: Int, itemsPerPage: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 1)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)

  def first = 1
  def last = (total / itemsPerPage).toInt + 1
}