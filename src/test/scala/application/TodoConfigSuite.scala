package es.eriktorr.todo
package application

import common.application.HealthConfig.{LivenessPath, ReadinessPath}
import common.application.HttpServerConfig.MaxActiveRequests
import common.application.{HealthConfig, HttpServerConfig}

import cats.implicits.catsSyntaxEitherId
import com.comcast.ip4s.{host, ip, port}
import com.monovore.decline.{Command, Help}
import io.github.iltotore.iron.*
import munit.FunSuite

import scala.concurrent.duration.DurationInt
import scala.util.Properties

final class TodoConfigSuite extends FunSuite:
  test("should load configuration from environment variables"):
    assume(Properties.envOrNone("SBT_TEST_ENV_VARS").nonEmpty, "this test runs only on sbt")
    assertEquals(
      Command(name = "name", header = "header")(TodoConfig.opts)
        .parse(List.empty, sys.env),
      TodoConfig(
        HealthConfig(
          LivenessPath("/liveness-path"),
          ReadinessPath("/readiness-path"),
        ),
        HttpServerConfig(
          host"localhost",
          MaxActiveRequests(1024L),
          port"8000",
          11.seconds,
        ),
      ).asRight[Help],
    )

  test("should find the local IP address"):
    assertNotEquals(TodoConfig.localIpAddress, ip"127.0.0.1")
