package container.bootcamp.ausleihen.books

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor
import container.bootcamp.ausleihen.books.Book.BookEvents._
import container.bootcamp.ausleihen.books.Book._


/**
  * Book actor which holds a state for a dedicated book only the update stuff.
  */
object Book {
  def props: Props = Props(new Book)
  case class BookData(id: Option[String], Isbn: String, Titel: String, Autor: String, KurzBeschreibung: String, lend: Boolean = false)
  case object BookLend
  case class BookLent(lend: Boolean)
  case object BookAlreadyLent
  case object BookReturn
  case class BookReturned(returned: Boolean)
  case object BookAlreadyReturned
  case object BookDataGet

  /*
   * Events which are persisted for a book
   */
  object BookEvents {
    sealed abstract class BookEvent
    case class BookIdUpdated(id: String) extends BookEvent
    case class BookIsbnUpdated(isbn: String) extends BookEvent
    case class BookTitleUpdated(title: String) extends BookEvent
    case class BookAuthorUpdated(author: String) extends BookEvent
    case class BookDescriptionUpdated(description: String) extends BookEvent
    case class BookLendUpdated(lend: Boolean) extends BookEvent

  }

}
class Book extends PersistentActor with ActorLogging {

  override def persistenceId: String = self.path.name

  var id: Option[String] = None
  var isbn: String = _
  var title: String = _
  var author: String = _
  var description: String = _
  var lent: Boolean = _

  def receiveCommand: PartialFunction[Any, Unit] = {
    case BookData(bdId, bdIsbn, bdTitle, bdAuthor, bdDescription, bdLend) =>

      bdId.foreach { id =>
        if(this.id != bdId) persist(BookIdUpdated(id)) { _ => this.id = bdId }
      }

      if(isbn != bdIsbn) persist(BookIsbnUpdated(bdIsbn)) {e => isbn = e.isbn}
      if(title != bdTitle) persist(BookTitleUpdated(bdTitle)) {e => title = e.title}
      if(author != bdAuthor) persist(BookAuthorUpdated(bdAuthor)) {e => author = e.author}
      if(description != bdDescription) persist(BookDescriptionUpdated(bdDescription)) {e => description = e.description}
      if(lent != bdLend) persist(BookLendUpdated(bdLend)) { e => lent = e.lend}

    case BookLend =>

      if(!lent) {
        persist(BookLendUpdated(true)) { e =>
            lent = e.lend
            sender ! BookLent(lent)
        }
      } else {
        sender ! BookAlreadyLent
      }

    case BookReturn =>

      if(lent) {
        persist(BookLendUpdated(false)) { e =>
            lent = e.lend
            sender ! BookReturned(lent)
        }
      } else {
        sender ! BookAlreadyReturned
      }
  }

  def receiveRecover: PartialFunction[Any, Unit] = {
    case BookIdUpdated(bdId) => id = Some(bdId)
    case BookIsbnUpdated(bdIsbn) => isbn = bdIsbn
    case BookTitleUpdated(bdTitle) => title = bdTitle
    case BookAuthorUpdated(bdAuthor) => author = bdAuthor
    case BookDescriptionUpdated(bdDescription) => description = bdDescription
    case BookLendUpdated(bdLend) => lent = bdLend
  }
}