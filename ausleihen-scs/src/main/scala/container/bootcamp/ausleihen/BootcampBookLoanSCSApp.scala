package container.bootcamp.ausleihen

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.persistence.pg.journal.query.PostgresReadJournal
import akka.persistence.query.PersistenceQuery
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.uber.jaeger.Configuration
import container.bootcamp.ausleihen.SSE.LendServerSendEvents
import container.bootcamp.ausleihen.books.Book._
import container.bootcamp.ausleihen.books.BookLibrarian.{BookNotFound, BookLent => BookLibrarianBookLent}
import container.bootcamp.ausleihen.books.BookLibrarianLookup.{BookDataLookup, BookGet, BookIsLent, BookLentState}
import container.bootcamp.ausleihen.books.{BookLibrarian, BookLibrarianLookup}
import container.bootcamp.ausleihen.util.OpentraceDirective._
import container.bootcamp.ausleihen.view.BookPages._
import io.opentracing.util.GlobalTracer
import slick.util.Logging

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Start the main book loan app.
  */
object BootcampBookLoanSCSApp extends App with Logging with LendServerSendEvents {

  implicit val system = ActorSystem("container-bootcamp-ausleihen")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher
  implicit val timeout = Timeout(10 seconds)

  val bookLibrarian = system.actorOf(BookLibrarian.props, "book-librarian")

  val readJournal = PersistenceQuery(system)
    .readJournalFor[PostgresReadJournal](PostgresReadJournal.Identifier)

  val bookLibrarianLookup = system.actorOf(BookLibrarianLookup.props(readJournal), "book-librarian-lookup")

  val headers = Seq(
    RawHeader("Cache-Control","no-cache, no-store, max-age=0, must-revalidate"),
    RawHeader("Pragma", "no-cache"),
    RawHeader("Expires", "Fri, 01 Jan 1990 00:00:00 GMT")
  )

  val tracer =
    new Configuration(
      "ausleihen",
      new Configuration.SamplerConfiguration("const", 1),
      new Configuration.ReporterConfiguration(true, "localhost", null, 500, 10000)
    ).getTracer

  GlobalTracer.register(tracer)


  val route = ignoreTrailingSlash {
    pathPrefix("book") {
      pathEndOrSingleSlash {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, indexPage))
        }
      } ~
        pathPrefix(Segment) { isbn =>
          opentrace("book") { span =>
            pathEndOrSingleSlash {
              get {
                opentrace("data", span.toOpt) { _ =>
                  respondWithHeaders(headers: _*) {
                    completeOrRecoverWith((bookLibrarianLookup ? BookGet(isbn)).map {
                      case data: BookDataLookup => HttpEntity(ContentTypes.`text/html(UTF-8)`, bookPage(data))
                      case notFound: BookNotFound => HttpEntity(ContentTypes.`text/html(UTF-8)`, bookNotFound(notFound))
                    }) { extraction =>
                      logger.error(extraction.toString)
                      complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, unexpectedErrorPage))
                    }
                  }
                }
              }
            } ~
              path("lent") {
                opentrace("lent", span.toOpt) { _ =>
                  pathEndOrSingleSlash {
                    post {
                      formFields("lent".as[Boolean]) { lentState =>
                        respondWithHeaders(headers: _*) {
                          completeOrRecoverWith((bookLibrarian ? BookLibrarianBookLent(isbn, lentState)).map {
                            case data: BookLent => HttpEntity(ContentTypes.`text/html(UTF-8)`, lentPage(data))
                            case data: BookReturned => HttpEntity(ContentTypes.`text/html(UTF-8)`, returnPage(data))
                            case _: BookAlreadyLent.type => HttpEntity(ContentTypes.`text/html(UTF-8)`, alreadyLendPage)
                            case _: BookAlreadyReturned.type => HttpEntity(ContentTypes.`text/html(UTF-8)`, alreadyReturnedPage)
                            case notFound: BookNotFound => HttpEntity(ContentTypes.`text/html(UTF-8)`, bookNotFound(notFound))
                          }) { extraction =>
                            logger.error(extraction.toString)
                            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, unexpectedErrorPage))
                          }
                        }
                      }
                    } ~
                      get {
                        completeOrRecoverWith((bookLibrarianLookup ? BookIsLent(isbn)).map {
                          case lentState: BookLentState => HttpEntity(ContentTypes.`text/html(UTF-8)`, lentStatePage(lentState.lend))
                          case notFound: BookNotFound => HttpEntity(ContentTypes.`text/html(UTF-8)`, bookNotFound(notFound))
                        }) { extraction =>
                          logger.error(extraction.toString)
                          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, unexpectedErrorPage))
                        }
                      }
                  }
                }
              }
          }
        }
    } ~
      pathPrefix("assets") {
        getFromResourceDirectory("assets")
      } ~
      path("events") {

        import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._
        optionalHeaderValueByName("Last-Event-ID") {
          case Some(id) =>
            complete {
              sseLent(id.toLong)
            }
          case None =>
            complete {
              sseLent(0L)
            }
        }

      }
  }

  Http().bindAndHandle(route, "0.0.0.0", AppConfig.ContainerBootcampAusleihenConfig.cbaPort)

}
