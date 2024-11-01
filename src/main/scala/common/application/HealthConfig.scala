package es.eriktorr.todo
package common.application

import common.application.HealthConfig.{LivenessPath, ReadinessPath}
import common.data.refined.Constraints.UrlPathSegment

import cats.Show
import io.github.iltotore.iron.*

final case class HealthConfig(livenessPath: LivenessPath, readinessPath: ReadinessPath)

object HealthConfig:
  opaque type LivenessPath <: String :| UrlPathSegment = String :| UrlPathSegment
  object LivenessPath extends RefinedTypeOps[String, UrlPathSegment, LivenessPath]

  opaque type ReadinessPath <: String :| UrlPathSegment = String :| UrlPathSegment
  object ReadinessPath extends RefinedTypeOps[String, UrlPathSegment, ReadinessPath]

  val defaultLivenessPath: LivenessPath = LivenessPath("/healthz")
  val defaultReadinessPath: ReadinessPath = ReadinessPath("/ready")

  given Show[HealthConfig] =
    import scala.language.unsafeNulls
    Show.show(config =>
      s"liveness-path: ${config.livenessPath}, readiness-path: ${config.readinessPath}",
    )
