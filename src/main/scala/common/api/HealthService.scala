package es.eriktorr.todo
package common.api

import common.api.HealthService.ServiceName
import common.application.HealthConfig
import common.application.HealthConfig.{LivenessPath, ReadinessPath}
import common.data.refined.Constraints.NonEmptyString

import cats.effect.{IO, Ref, Resource}
import io.github.iltotore.iron.*
import org.typelevel.log4cats.Logger

trait HealthService:
  def isReady: IO[Boolean]
  def livenessPath: LivenessPath
  def markReady: IO[Unit]
  def markUnready: IO[Unit]
  def readinessPath: ReadinessPath
  def serviceName: ServiceName

object HealthService:
  opaque type ServiceName <: String :| NonEmptyString = String :| NonEmptyString

  object ServiceName extends RefinedTypeOps[String, NonEmptyString, ServiceName]

  def resourceWith(healthConfig: HealthConfig, serviceName: ServiceName, ready: Boolean = false)(
      using logger: Logger[IO],
  ): Resource[IO, HealthService] =
    Resource.make(for
      readyRef <- Ref.of[IO, Boolean](ready)
      _serviceName = serviceName
      healthService = new HealthService():
        override def isReady: IO[Boolean] = readyRef.get

        override def livenessPath: LivenessPath = healthConfig.livenessPath

        override def markReady: IO[Unit] = for
          _ <- logger.info("HealthService marked as ready")
          _ <- readyRef.set(true)
        yield ()

        override def markUnready: IO[Unit] = for
          _ <- logger.info("HealthService marked as unready")
          _ <- readyRef.set(false)
        yield ()

        override def readinessPath: ReadinessPath = healthConfig.readinessPath

        override def serviceName: ServiceName = _serviceName
    yield healthService)(_.markUnready)
