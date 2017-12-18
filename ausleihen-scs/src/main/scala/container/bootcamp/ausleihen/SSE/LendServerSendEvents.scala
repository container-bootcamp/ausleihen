package container.bootcamp.ausleihen.SSE

import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.persistence.query.EventEnvelope
import akka.stream.ActorMaterializer
import container.bootcamp.ausleihen.BootcampBookLoanSCSApp.readJournal
import container.bootcamp.ausleihen.books.Book.BookEvents.BookLentUpdated
import container.bootcamp.ausleihen.util.JsonMapper

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._


trait LendServerSendEvents {

  def materializer: ActorMaterializer
  def ec: ExecutionContext

  def extractIsbn(persistenceId: String): String = {
    persistenceId.split("-").last
  }

  case class SSEBookLentUpdated(isbn: String, lent: Boolean)

  def sseLent(offset: Long) = {

    readJournal.allEvents(offset).map {
      case EventEnvelope(envOffset, id, _, event: BookLentUpdated) =>
        Option(ServerSentEvent(
          JsonMapper.toJson(SSEBookLentUpdated(extractIsbn(id), event.lend)),
          "book-lent-updated",
          envOffset.toString
        ))
      case _ => None
    }.dropWhile(e => e.isEmpty).map(_.get).keepAlive(5.second, () => ServerSentEvent.heartbeat)


  }

}
