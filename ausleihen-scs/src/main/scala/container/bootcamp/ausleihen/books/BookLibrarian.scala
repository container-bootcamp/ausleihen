package container.bootcamp.ausleihen.books

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import akka.stream.ActorMaterializer
import akka.stream.alpakka.sse.scaladsl.EventSource
import container.bootcamp.ausleihen.AppConfig.ContainerBootcampEinbuchenConfig._
import container.bootcamp.ausleihen.books.Book._
import container.bootcamp.ausleihen.books.BookLibrarian.BookLibrarianEvents.{BookIsbnReceived, MessageReceived}
import container.bootcamp.ausleihen.books.BookLibrarian.{BookNotFound, BookLent => BookLibrarianBookLent}
import container.bootcamp.ausleihen.util.JsonMapper
import BookLibrarian.createBookName

import scala.concurrent.Future
import scala.concurrent.duration._


/**
  * Know all existing books and manages them
  * e.g receive new books from einbuchen or
  * update the lent state of a book
  */
object BookLibrarian {
  def props: Props = Props(new BookLibrarian)
  case class BookLent(isbn: String, lentState: Boolean)
  case class BookReturn(isbn: String)
  case class BookNotFound(isbn: String)
  val namePrefix = "book-isbn"

  def createBookName(isbn: String): String = s"$namePrefix-$isbn"

  object BookLibrarianEvents {
    case class MessageReceived(id: Option[String])
    case class BookIsbnReceived(isbn: String)
  }
}
class BookLibrarian extends PersistentActor with ActorLogging {

  implicit val system = context.system
  implicit val materializer = ActorMaterializer.create(context)

  override def persistenceId: String = self.path.name

  /*
   * Hold the last received sse message id. So on restart only the
   * difference has to be load
   */
  var lastMessageId: Option[String] = None

  /*
   * All book isbn's managed by the book librarian. When apps restart
   * only for the new books an book actor is created. If then a request
   * for on older book comes in the book isn't known. With this isbn
   * directory a look up is made whether the books already exists.
   */
  var knownBooks = Set.empty[String]

  /*
   * Crate a new book actor which holds and persist the book state.
   */
  private def createBook(isbn: String) = context.actorOf(Book.props, createBookName(isbn))

  /*
   * look up for a book and return the corresponding actorRef
   */
  private def findChild(isbn: String)(f: ActorRef => Unit) = {
    context.child(createBookName(isbn)) match {
      case Some(actor) => f(actor)
      case None =>

        if(knownBooks.contains(isbn)){
          f(createBook(isbn))
        }
        else {
          sender ! BookNotFound(isbn)
        }
    }
  }

  /*
   * Function with for fetch events from einbuchen service
   */
  private def send(request: HttpRequest): Future[HttpResponse] = Http().singleRequest(request)

  def receiveCommand: PartialFunction[Any, Unit] = {
    case ServerSentEvent(data, Some("BuchEingebucht"), Some(id), _ ) =>
      persist(MessageReceived(Some(id))){ e =>
        val bookData = JsonMapper.fromJson[BookData](data).copy(id = e.id)
        persist(BookIsbnReceived(bookData.Isbn)) {
          e =>
            knownBooks += e.isbn
            context.child(createBookName(bookData.Isbn)).getOrElse(createBook(bookData.Isbn)) ! bookData
            log.debug("create book with data: " + bookData)
        }

      }
    case BookLibrarianBookLent(isbn, lentState) =>
      findChild(isbn) {
        book =>

          if(lentState) {
            book forward BookLend
          }
          else {
            book forward BookReturn
          }
      }

  }

  def receiveRecover: PartialFunction[Any, Unit] = {
    case MessageReceived(id) => lastMessageId = id
    case BookIsbnReceived(isbn) => knownBooks += isbn
    case RecoveryCompleted =>
      EventSource(Uri(cbeUrl), send, lastMessageId, 1.second)
      .runForeach(serverSentEvent => self ! serverSentEvent)
    case event => log.debug("Unexpected:" + event)
  }
}

