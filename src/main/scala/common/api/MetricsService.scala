package es.eriktorr.todo
package common.api

import cats.effect.{IO, Resource}
import org.http4s.HttpRoutes
import org.http4s.metrics.prometheus.{Prometheus, PrometheusExportService}
import org.http4s.server.middleware.Metrics

trait MetricsService:
  def metricsFor(routes: HttpRoutes[IO]): HttpRoutes[IO]

  def prometheusExportRoutes: HttpRoutes[IO]

object MetricsService:
  def resourceWith(
      prefix: String,
      prometheusExportService: PrometheusExportService[IO],
  ): Resource[IO, MetricsService] =
    Prometheus.metricsOps[IO](prometheusExportService.collectorRegistry, prefix).map { metricsOps =>
      new MetricsService():
        override def metricsFor(routes: HttpRoutes[IO]): HttpRoutes[IO] =
          Metrics[IO](metricsOps)(routes)

        override def prometheusExportRoutes: HttpRoutes[IO] = prometheusExportService.routes
    }
