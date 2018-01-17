package container.bootcamp.ausleihen.books

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.persistence.pg.journal.query.PostgresReadJournal
import akka.persistence.query.EventEnvelope
import akka.stream.ActorMaterializer
import container.bootcamp.ausleihen.books.Book.BookEvents._
import container.bootcamp.ausleihen.books.BookLibrarian.{BookNotFound, createBookName}
import container.bootcamp.ausleihen.books.BookLibrarianLookup._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
  * BookLibrarianLookup answers all questions concerning books.
  * eg. request for bookData or lent state
  */
object BookLibrarianLookup {
  def props(readJournal: PostgresReadJournal): Props = Props(new BookLibrarianLookup(readJournal))
  sealed abstract class BookDataLookupResult
  case class BookIsLent(isbn:String)
  case class BookLentState(lend: Boolean) extends BookDataLookupResult
  case class BookDataLookup(id: Option[String] = None,
                            isbn: Option[String] = None,
                            title: Option[String] = None,
                            author: Option[String] = None,
                            shortDescription: Option[String] = None,
                            lent: Boolean = false,
                            reserved: Boolean = false) extends BookDataLookupResult

  case class BookGet(isbn: String)
}
class BookLibrarianLookup(readJournal: PostgresReadJournal) extends Actor with ActorLogging {

  implicit val materializer: ActorMaterializer = ActorMaterializer.create(context)
  implicit val ec: ExecutionContext = context.system.dispatcher

  var lastsequenceNr = Map.empty[String, Long].withDefaultValue(0L)

  /*
   * Read all book events to keep the latest sequence number of a persistence id in mind
   * Stream never ends.
   *
   * This is needed due on query the exact last seqNr have to provided otherwise the the stream never stops on a
   * request.
   */
  readJournal.allEvents(0L).runForeach {
    case EventEnvelope(_, id, seqNo, _: BookEvent) => lastsequenceNr = lastsequenceNr + (id -> seqNo)
    case unmatchedEnvelope: EventEnvelope => log.debug("unused event" + unmatchedEnvelope.event.getClass.getName)
  }

  private def lentTransform(bookDataLookup: BookDataLookup) = {
    BookLentState(bookDataLookup.lent)
  }

  private def bookDataTransform(bookDataLookup: BookDataLookup) = {
    bookDataLookup
  }


  private def fetchBook(isbn: String, caller: ActorRef, transform:(BookDataLookup) => BookDataLookupResult): Unit = {

    val bookIdentifier = createBookName(isbn)

    if(lastsequenceNr.contains(bookIdentifier)) {
      val latest = lastsequenceNr(bookIdentifier)

      readJournal.eventsByPersistenceId(bookIdentifier, 0L, latest).runFold(BookDataLookup()) {
        (accu, envelope) =>
          readBook(accu, envelope)
      }.onComplete {
        case Success(fetchedBook) =>
          caller ! transform(fetchedBook)
        case Failure(fail) =>
          log.error("unexpected failure:" + fail.toString)
          caller ! BookNotFound
      }
    }

  }

  def receive: PartialFunction[Any, Unit] = {
    case BookIsLent(isbn) =>

        fetchBook(isbn, sender, lentTransform)

    case BookGet(isbn) =>

        fetchBook(isbn, sender, bookDataTransform)

  }

  private def readBook(book: BookDataLookup, envelope: EventEnvelope): BookDataLookup = {

    envelope match {
      case EventEnvelope(_, _, _, event: BookIdUpdated) =>
        book.copy(id = Option(event.id))
      case EventEnvelope(_, _, _, event: BookIsbnUpdated) =>
        book.copy(isbn = Option(event.isbn))
      case EventEnvelope(_, _, _, event: BookTitleUpdated) =>
        book.copy(title = Option(event.title))
      case EventEnvelope(_, _, _, event: BookAuthorUpdated) =>
        book.copy(author = Option(event.author))
      case EventEnvelope(_, _, _, event: BookDescriptionUpdated) =>
        book.copy(shortDescription = Option(event.description))
      case EventEnvelope(_, _, _, event: BookLentUpdated) =>
        book.copy(lent = event.lend)
      case EventEnvelope(_, _,_, event: BookReservedUpdated) =>
        book.copy(reserved = event.reserved)
      case unmatchedEnvelope: EventEnvelope =>
        log.debug("unused event" + unmatchedEnvelope.event.getClass.getName)
        book
    }

  }
}


