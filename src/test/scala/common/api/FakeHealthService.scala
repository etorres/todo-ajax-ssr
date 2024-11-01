package es.eriktorr.todo
package common.api

import common.api.FakeHealthService.HealthServiceState
import common.api.HealthService.ServiceName
import common.application.HealthConfig
import common.application.HealthConfig.{LivenessPath, ReadinessPath}

import cats.effect.{IO, Ref}
import io.github.iltotore.iron.*

final class FakeHealthService(stateRef: Ref[IO, HealthServiceState]) extends HealthService:
  override def isReady: IO[Boolean] = IO.pure(true)

  override def livenessPath: LivenessPath = HealthConfig.defaultLivenessPath

  override def markReady: IO[Unit] = stateRef.update(_.copy(true))

  override def markUnready: IO[Unit] = stateRef.update(_.copy(false))

  override def readinessPath: ReadinessPath = HealthConfig.defaultReadinessPath

  override def serviceName: ServiceName = ServiceName("ServiceName")

object FakeHealthService:
  final case class HealthServiceState(ready: Boolean)

  object HealthServiceState:
    val unready: HealthServiceState = HealthServiceState(false)
