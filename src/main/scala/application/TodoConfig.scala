package es.eriktorr.todo
package application

import common.application.HealthConfig.{LivenessPath, ReadinessPath}
import common.application.HttpServerConfig.MaxActiveRequests
import common.application.argument.HttpServerConfigArgument.given
import common.application.{HealthConfig, HttpServerConfig}

import cats.Show
import cats.implicits.{catsSyntaxTuple2Semigroupal, catsSyntaxTuple4Semigroupal, showInterpolator}
import com.comcast.ip4s.{ipv4, Host, IpAddress, Port}
import com.monovore.decline.Opts
import io.github.iltotore.iron.decline.given

import scala.concurrent.duration.FiniteDuration
import scala.util.Using

final case class TodoConfig(healthConfig: HealthConfig, httpServerConfig: HttpServerConfig)

object TodoConfig:
  given Show[TodoConfig] =
    import scala.language.unsafeNulls
    Show.show(config => show"""[${config.healthConfig},
                              | ${config.httpServerConfig}]""".stripMargin.replaceAll("\\R", ""))

  def opts: Opts[TodoConfig] =
    val healthConfig = (
      Opts
        .env[LivenessPath](
          name = "TODO_HEALTH_LIVENESS_PATH",
          help = "Set liveness path.",
        )
        .withDefault(HealthConfig.defaultLivenessPath),
      Opts
        .env[ReadinessPath](
          name = "TODO_HEALTH_READINESS_PATH",
          help = "Set readiness path.",
        )
        .withDefault(HealthConfig.defaultReadinessPath),
    ).mapN(HealthConfig.apply)

    val httpServerConfig = (
      Opts
        .env[Host](name = "TODO_HTTP_HOST", help = "Set HTTP host.")
        .withDefault(HttpServerConfig.defaultHost),
      Opts
        .env[MaxActiveRequests](
          name = "TODO_HTTP_MAX_ACTIVE_REQUESTS",
          help = "Set HTTP max active requests.",
        )
        .withDefault(HttpServerConfig.defaultMaxActiveRequests),
      Opts
        .env[Port](name = "TODO_HTTP_PORT", help = "Set HTTP port.")
        .withDefault(HttpServerConfig.defaultPort),
      Opts
        .env[FiniteDuration](name = "TODO_HTTP_TIMEOUT", help = "Set HTTP timeout.")
        .withDefault(HttpServerConfig.defaultTimeout),
    ).mapN(HttpServerConfig.apply)

    (healthConfig, httpServerConfig).mapN(TodoConfig.apply)

  def localIpAddress: IpAddress = Using(java.net.DatagramSocket()): datagramSocket =>
    import scala.language.unsafeNulls
    datagramSocket.connect(java.net.InetAddress.getByName("8.8.8.8"), 12345)
    datagramSocket.getLocalAddress.getHostAddress
  .toOption.flatMap(IpAddress.fromString).getOrElse(ipv4"127.0.0.1")
