package container.bootcamp.ausleihen.util

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.{extractRequest, mapResponse, provide}
import io.opentracing.ActiveSpan
import io.opentracing.util.GlobalTracer

/**
  * Directive to push spans to opentrace jaeger implementation
  */
sealed trait OpentraceDirective {

  def trace(spanName: String, parent: Option[ActiveSpan] = None): Directive1[ActiveSpan] = {
    extractRequest.flatMap { req =>

      val span = parent match {
        case Some(p) => GlobalTracer.get().buildSpan(spanName).asChildOf(p).startActive()
        case None => GlobalTracer.get().buildSpan(spanName).startActive()
      }

      val isbn = req.getUri.query.getOrElse("isbn","no param")

      mapResponse { resp =>
        span.log("book data")
        span.setTag("isbn", isbn)
        span.close()
        resp
      } & provide (span)
    }
  }

}
object OpentraceDirective extends OpentraceDirective {

  implicit class OpentraceDirectiveImplicits(span: ActiveSpan) {
    def toOpt: Option[ActiveSpan] = Option(span)
  }

  def opentrace(spanName:String, parent:Option[ActiveSpan] = None): Directive1[ActiveSpan] =
    trace(spanName, parent)
}





