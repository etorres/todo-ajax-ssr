package es.eriktorr.todo

import application.{TodoConfig, TodoHttpApp, TodoParams}
import common.api.HealthService.ServiceName
import common.api.template.TemplateEngine
import common.api.{HealthService, MetricsService}
import common.application.HttpServer
import tasks.domain.TasksService

import cats.effect.{ExitCode, IO, Resource}
import cats.implicits.{catsSyntaxTuple2Semigroupal, showInterpolator}
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp
import org.http4s.metrics.prometheus.PrometheusExportService
import org.http4s.server.middleware.{MaxActiveRequests, Timeout}
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object TodoApp extends CommandIOApp(name = "todo-http", header = "TO-DO application"):
  override def main: Opts[IO[ExitCode]] =
    (TodoConfig.opts, TodoParams.opts).mapN { case (config, params) =>
      program(config, params)
    }

  private def program(config: TodoConfig, params: TodoParams) = for
    logger <- Slf4jLogger.create[IO]
    given SelfAwareStructuredLogger[IO] = logger
    _ <- logger.info(show"Starting HTTP server with configuration: $config")
    _ <- (for
      healthService <- HealthService.resourceWith(
        config.healthConfig,
        ServiceName.applyUnsafe("TO-DO application"),
      )
      prometheusExportService <- PrometheusExportService.build[IO]
      metricsService <- MetricsService.resourceWith("http4s_server", prometheusExportService)
      tasksService <- TasksService.build
      templateEngine = TemplateEngine.html[IO]()
      httpApp <- Resource.eval:
        MaxActiveRequests
          .forHttpApp[IO](config.httpServerConfig.maxActiveRequests)
          .map: middleware =>
            // Limit the number of active requests by rejecting requests over the limit defined
            middleware(
              TodoHttpApp(
                healthService,
                metricsService,
                tasksService,
                templateEngine,
                params.verbose,
              ).httpApp,
            )
          .map: decoratedHttpApp =>
            // Limit how long the underlying service takes to respond
            Timeout.httpApp[IO](timeout = config.httpServerConfig.timeout)(decoratedHttpApp)
      _ <- HttpServer.impl(httpApp, config.httpServerConfig)
    yield healthService).use: healthService =>
      healthService.markReady.flatMap(_ => IO.never[Unit])
  yield ExitCode.Success
