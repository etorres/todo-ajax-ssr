package es.eriktorr.todo
package tasks.domain

import tasks.domain.Task.*
import tasks.domain.TasksService.{Filter, TaskRequest}

import cats.effect.{IO, Ref, Resource}

trait TasksService:
  def add(taskRequest: TaskRequest): IO[Unit]
  def delete(filter: Filter): IO[Unit]
  def find(id: Id): IO[Option[Task]]
  def list(): IO[List[Task]]
  def update(task: Task): IO[Unit]

object TasksService:
  def build: Resource[IO, TasksService] =
    for
      stateRef <- Resource.eval(
        Ref.of[IO, List[Task]](
          List(
            Task(
              id = Id.applyUnsafe(1),
              label = Label.applyUnsafe("Something that should be done"),
              completed = true,
            ),
            Task(
              id = Id.applyUnsafe(2),
              label = Label.applyUnsafe("Ops we missed this one"),
            ),
          ),
        ),
      )
      taskService = new TasksService():
        override def add(taskRequest: TaskRequest): IO[Unit] = stateRef.update { currentState =>
          val id = currentState.map(_.id).sorted.reverse.headOption.getOrElse(Id.applyUnsafe(0))
          taskRequest.toTask(Id.applyUnsafe(id + 1)) :: currentState
        }
        override def delete(filter: Filter): IO[Unit] = filter match
          case Filter.Completed => stateRef.update(_.filterNot(_.completed))
        override def find(id: Id): IO[Option[Task]] = stateRef.get.map(_.find(_.id == id))
        override def list(): IO[List[Task]] = stateRef.get.map(identity)
        override def update(task: Task): IO[Unit] =
          stateRef.update(currentState => task :: currentState.filterNot(_.id == task.id))
    yield taskService

  sealed trait Filter

  object Filter:
    case object Completed extends Filter

  final case class TaskRequest(label: Label, completed: Boolean):
    def toTask(id: Id): Task = Task(id, label, completed)
