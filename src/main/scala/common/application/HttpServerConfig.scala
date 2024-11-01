package es.eriktorr.todo
package common.application

import common.application.HttpServerConfig.MaxActiveRequests
import common.data.refined.Constraints.Between

import cats.Show
import com.comcast.ip4s.{ipv4, port, Host, Port}
import io.github.iltotore.iron.*

import scala.concurrent.duration.{DurationInt, FiniteDuration}

final case class HttpServerConfig(
    host: Host,
    maxActiveRequests: MaxActiveRequests,
    port: Port,
    timeout: FiniteDuration,
)

object HttpServerConfig:
  opaque type MaxActiveRequests <: Long :| Between[1L, 4096L] = Long :| Between[1L, 4096L]

  object MaxActiveRequests extends RefinedTypeOps[Long, Between[1L, 4096L], MaxActiveRequests]

  val defaultHost: Host = ipv4"0.0.0.0"
  val defaultMaxActiveRequests: MaxActiveRequests = MaxActiveRequests(128L)
  val defaultPort: Port = port"8080"
  val defaultTimeout: FiniteDuration = 40.seconds

  val default: HttpServerConfig =
    HttpServerConfig(defaultHost, defaultMaxActiveRequests, defaultPort, defaultTimeout)

  given Show[HttpServerConfig] =
    import scala.language.unsafeNulls
    Show.show(config => s"""http-host: ${config.host},
                           | http-max-active-requests: ${config.maxActiveRequests},
                           | http-port: ${config.port},
                           | http-timeout: ${config.timeout}""".stripMargin.replaceAll("\\R", ""))
