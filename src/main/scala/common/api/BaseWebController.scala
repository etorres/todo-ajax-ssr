package es.eriktorr.todo
package common.api

import common.api.BaseWebController.InvalidRequest
import common.api.template.{TemplateContext, TemplateEngine}
import common.data.error.HandledError

import cats.effect.IO
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import org.http4s.{Charset, HttpRoutes, MediaType, Request, Response}
import org.typelevel.ci.CIStringSyntax
import org.typelevel.log4cats.SelfAwareStructuredLogger

abstract class BaseWebController(templateEngine: TemplateEngine[IO]):
  val routes: Option[HttpRoutes[IO]] = None

  protected def contextFrom(request: Request[IO])(using
      logger: SelfAwareStructuredLogger[IO],
  ): Throwable => IO[Response[IO]] =
    (error: Throwable) =>
      val requestId = request.headers.get(ci"X-Request-ID").map(_.head.value)
      val context = Map("http.request.id" -> requestId.getOrElse("null"))
      error match
        case invalidRequest: InvalidRequest =>
          logger.error(context, invalidRequest)("Invalid request") *> BadRequest()
        case other =>
          logger.error(context, other)(
            "Unhandled error raised while handling event",
          ) *> InternalServerError()

  private val emptyResponse =
    Ok().map(_.withContentType(`Content-Type`(MediaType.text.html, Charset.`UTF-8`)))

  protected def responseFrom(
      name: String,
      request: Request[IO],
      variable: (String, AnyRef),
  ): IO[Response[IO]] = for
    response <- emptyResponse
    context = TemplateContext.from[IO](request, response, variable)
    html <- templateEngine.htmlFrom(context, name)
  yield response.withBodyStream(fs2.Stream.emits(html.getBytes.nn))

object BaseWebController:
  final case class InvalidRequest(cause: Throwable)
      extends HandledError("Invalid request", Some(cause))
