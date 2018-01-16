package container.bootcamp.ausleihen.SSE

import akka.NotUsed
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.persistence.pg.journal.query.PostgresReadJournal
import akka.persistence.query.EventEnvelope
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import container.bootcamp.ausleihen.books.Book.BookEvents.BookLentUpdated
import container.bootcamp.ausleihen.util.JsonMapper

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._


trait LendServerSendEvents {

  def materializer: ActorMaterializer
  def ec: ExecutionContext
  def readJournal: PostgresReadJournal

  def extractIsbn(persistenceId: String): String = {
    persistenceId.split("-").last
  }

  case class SSEBookLentUpdated(isbn: String, lent: Boolean)

  def sseLent(offset: Long): Source[ServerSentEvent, NotUsed] = {

    readJournal.eventsByTags(Set("book" -> "book-lent-updated"),offset).map {
      case EventEnvelope(envOffset, id, _, event: BookLentUpdated) =>
        ServerSentEvent(
          JsonMapper.toJson(SSEBookLentUpdated(extractIsbn(id), event.lend)),
          "book-lent-updated",
          envOffset.toString
        )
    }.keepAlive(5.second, () => ServerSentEvent.heartbeat)

  }

}
