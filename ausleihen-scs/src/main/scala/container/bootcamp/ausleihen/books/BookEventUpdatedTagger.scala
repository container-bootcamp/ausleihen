package container.bootcamp.ausleihen.books


import container.bootcamp.ausleihen.books.Book.BookEvents.BookLentUpdated
import akka.persistence.pg.event.EventTagger

class BookEventUpdatedTagger extends EventTagger {
  def tags(event: Any): Map[String, String] = {
    event match {
      case _: BookLentUpdated => Map("book" -> "book-lent-updated")
      case _ => Map.empty
    }
  }
}
