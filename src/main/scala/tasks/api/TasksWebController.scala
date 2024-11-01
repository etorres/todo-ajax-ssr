package es.eriktorr.todo
package tasks.api

import common.api.BaseWebController
import common.api.BaseWebController.InvalidRequest
import common.api.template.TemplateEngine
import tasks.domain.Task.{Id, Label}
import tasks.domain.TasksService
import tasks.domain.TasksService.Filter.Completed
import tasks.domain.TasksService.TaskRequest

import cats.effect.IO
import cats.implicits.catsSyntaxEither
import org.http4s.dsl.io.*
import org.http4s.{HttpRoutes, Request, UrlForm}
import org.typelevel.log4cats.SelfAwareStructuredLogger

import scala.jdk.CollectionConverters.SeqHasAsJava

final class TasksWebController(
    tasksService: TasksService,
    templateEngine: TemplateEngine[IO],
)(using logger: SelfAwareStructuredLogger[IO])
    extends BaseWebController(templateEngine):
  override val routes: Option[HttpRoutes[IO]] = Some(
    HttpRoutes.of[IO]:
      case request @ GET -> Root / "tasks" =>
        (for
          tasks <- tasksService.list()
          response <- responseFrom(
            name = "index",
            request = request,
            variable = "todos" -> tasks.map(TaskResponse.fromTask).asJava,
          )
        yield response).handleErrorWith(contextFrom(request))
      case request @ PATCH -> Root / "tasks" / taskId =>
        (for
          id <- idFrom(taskId)
          completed <- completedFrom(request)
          maybeTask <- tasksService.find(id)
          _ <- maybeTask match
            case Some(task) => tasksService.update(task.copy(completed = completed))
            case None => IO.unit
          response <- NoContent()
        yield response).handleErrorWith(contextFrom(request))
      case request @ DELETE -> Root / "tasks:cleanup" =>
        (for
          _ <- tasksService.delete(Completed)
          tasks <- tasksService.list()
          response <- responseFrom(
            name = "lines",
            request = request,
            variable = "todos" -> tasks.map(TaskResponse.fromTask).asJava,
          )
        yield response).handleErrorWith(contextFrom(request))
      case request @ POST -> Root / "tasks" =>
        (for
          maybeLabel <- maybeLabelFrom(request)
          response <- maybeLabel match
            case Some(label) =>
              for
                _ <- tasksService.add(TaskRequest(label, false))
                tasks <- tasksService.list()
                response <- responseFrom(
                  name = "table",
                  request = request,
                  variable = "todos" -> tasks.map(TaskResponse.fromTask).asJava,
                )
              yield response
            case None => NoContent()
        yield response).handleErrorWith(contextFrom(request)),
  )

  private def completedFrom(request: Request[IO]) = for
    form <- request.as[UrlForm]
    maybeChecked = form.get("checked").headOption
    completed <- IO.fromOption(maybeChecked.flatMap(_.toBooleanOption))(
      InvalidRequest(IllegalArgumentException("Expected true/false")),
    )
  yield completed

  private def idFrom(value: String) = IO.fromEither(
    Id.eitherString(value)
      .leftMap(error => InvalidRequest(IllegalArgumentException(error))),
  )

  private def maybeLabelFrom(request: Request[IO]) = for
    form <- request.as[UrlForm]
    maybeLabel = form.get("label").headOption.flatMap(Label.option)
  yield maybeLabel
