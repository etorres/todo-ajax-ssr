package es.eriktorr.todo
package application

import common.api.template.TemplateEngine
import common.api.{HealthService, MetricsService}
import tasks.api.TasksWebController
import tasks.domain.TasksService

import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits.toSemigroupKOps
import org.http4s.dsl.io.*
import org.http4s.server.Router
import org.http4s.server.middleware.{GZip, Logger as Http4sLogger, RequestId}
import org.http4s.server.staticcontent.WebjarServiceBuilder.WebjarAsset
import org.http4s.server.staticcontent.{resourceServiceBuilder, webjarServiceBuilder}
import org.http4s.{HttpApp, HttpRoutes, Response, Status}
import org.typelevel.log4cats.SelfAwareStructuredLogger

import scala.annotation.tailrec
import scala.util.chaining.scalaUtilChainingOps

final class TodoHttpApp(
    healthService: HealthService,
    metricsService: MetricsService,
    tasksService: TasksService,
    templateEngine: TemplateEngine[IO],
    enableLogger: Boolean = false,
)(using logger: SelfAwareStructuredLogger[IO]):
  private val maybeApiEndpoint =
    val endpoints = List(TasksWebController(tasksService, templateEngine))

    @tailrec
    def composedHttpRoutes(
        aggregated: HttpRoutes[IO],
        routes: List[HttpRoutes[IO]],
    ): HttpRoutes[IO] = routes match
      case Nil => aggregated
      case ::(head, next) => composedHttpRoutes(head <+> aggregated, next)

    NonEmptyList
      .fromList(endpoints.map(_.routes).collect { case Some(value) => value })
      .map(nel => composedHttpRoutes(nel.head, nel.tail))
      .map(routes =>
        metricsService
          .metricsFor(routes)
          .pipe: routes =>
            // Allow the compression of the Response body using GZip
            GZip(routes)
          .pipe: routes =>
            // Automatically generate a X-Request-ID header for a request, if one wasn't supplied
            RequestId.httpRoutes(routes)
          .pipe: routes =>
            // Log requests and responses
            if enableLogger then
              Http4sLogger.httpRoutes(
                logHeaders = true,
                logBody = true,
                redactHeadersWhen = _ => false,
                logAction = Some((msg: String) => logger.info(msg)),
              )(routes)
            else routes,
      )

  val httpApp: HttpApp[IO] =
    val favicon = resourceServiceBuilder[IO]("/WEB-INF/assets/icon").toRoutes

    val livenessCheckEndpoint: HttpRoutes[IO] = HttpRoutes.of[IO] { case GET -> Root =>
      Ok(s"${healthService.serviceName} is live")
    }

    val readinessCheckEndpoint: HttpRoutes[IO] = HttpRoutes.of[IO] { case GET -> Root =>
      healthService.isReady.ifM(
        ifTrue = Ok(s"${healthService.serviceName} is ready"),
        ifFalse = ServiceUnavailable(s"${healthService.serviceName} is not ready"),
      )
    }

    def isCssOrJsAsset(asset: WebjarAsset): Boolean =
      asset.asset.endsWith(".css") || asset.asset.endsWith(".js")

    val webJars: HttpRoutes[IO] =
      webjarServiceBuilder[IO].withWebjarAssetFilter(isCssOrJsAsset).toRoutes

    (maybeApiEndpoint match
      case Some(apiEndpoint) =>
        Router(
          "/todo" -> apiEndpoint,
          healthService.livenessPath -> livenessCheckEndpoint,
          healthService.readinessPath -> readinessCheckEndpoint,
          "/" -> (favicon <+> metricsService.prometheusExportRoutes <+> webJars),
        )
      case None =>
        Router(
          "/" -> HttpRoutes.of[IO] { case _ => IO.delay(Response(Status.InternalServerError)) },
        )
    ).orNotFound
