package es.eriktorr.todo
package common.api

import application.TodoHttpApp
import common.api.FakeHealthService.HealthServiceState
import tasks.domain.FakeTasksService.TasksServiceState
import tasks.domain.{FakeTasksService, Task}

import cats.effect.{IO, Ref}
import es.eriktorr.todo.common.api.template.TemplateEngine
import io.github.iltotore.iron.constraint.string.ValidUUID
import io.github.iltotore.iron.refineOption
import org.http4s.server.middleware.RequestId
import org.http4s.{EntityDecoder, Request, Status}
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object HttpAppSuiteRunner:
  final case class HttpAppState(
      healthServiceState: HealthServiceState,
      tasksServiceState: TasksServiceState,
  ):
    def setTasks(tasks: List[Task]): HttpAppState =
      copy(tasksServiceState = tasksServiceState.set(tasks))

  object HttpAppState:
    val empty: HttpAppState = HttpAppState(
      HealthServiceState.unready,
      TasksServiceState.empty,
    )

  def runWith[A](
      initialState: HttpAppState,
      request: Request[IO],
      templateEngine: TemplateEngine[IO] = TemplateEngine.html[IO](),
  )(using
      entityDecoder: EntityDecoder[IO, A],
  ): IO[(Either[Throwable, (A, Status)], HttpAppState)] = for
    tasksServiceStateRef <- Ref.of[IO, TasksServiceState](
      initialState.tasksServiceState,
    )
    healthServiceStateRef <- Ref.of[IO, HealthServiceState](initialState.healthServiceState)
    tasksService = FakeTasksService(tasksServiceStateRef)
    healthService = FakeHealthService(healthServiceStateRef)
    metricsService = FakeMetricsService()
    httpApp =
      given SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]
      TodoHttpApp(healthService, metricsService, tasksService, templateEngine).httpApp
    result <- (for
      response <- httpApp.run(request)
      status = response.status
      body <- status match
        case Status.Ok => response.as[A]
        case other =>
          IO.raiseError(IllegalStateException(s"Unexpected response status: ${other.code}"))
      _ <- IO.fromOption(for
        requestId <- response.attributes.lookup(RequestId.requestIdAttrKey)
        _ <- requestId.refineOption[ValidUUID]
      yield ())(IllegalStateException("Request Id not found"))
    yield (body, status)).attempt
    finalTasksServiceState <- tasksServiceStateRef.get
    finalHealthServiceState <- healthServiceStateRef.get
    finalState = initialState.copy(
      tasksServiceState = finalTasksServiceState,
      healthServiceState = finalHealthServiceState,
    )
    _ = result match
      case Left(error) => error.printStackTrace()
      case _ => ()
  yield (result, finalState)
