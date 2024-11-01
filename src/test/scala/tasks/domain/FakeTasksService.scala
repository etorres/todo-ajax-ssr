package es.eriktorr.todo
package tasks.domain

import tasks.domain.FakeTasksService.TasksServiceState
import tasks.domain.Task.Id
import tasks.domain.TasksService.{Filter, TaskRequest}

import cats.effect.{IO, Ref}

final class FakeTasksService(stateRef: Ref[IO, TasksServiceState]) extends TasksService:
  override def add(taskRequest: TaskRequest): IO[Unit] = stateRef.update { currentState =>
    val id = currentState.tasks.map(_.id).sorted.reverse.headOption.getOrElse(Id.applyUnsafe(0))
    currentState.copy(taskRequest.toTask(id) :: currentState.tasks)
  }

  override def delete(filter: Filter): IO[Unit] = filter match
    case Filter.Completed =>
      stateRef.update(currentState => currentState.copy(currentState.tasks.filterNot(_.completed)))

  override def find(id: Id): IO[Option[Task]] = stateRef.get.map(_.tasks.find(_.id == id))

  override def list(): IO[List[Task]] = stateRef.get.map(_.tasks)

  override def update(task: Task): IO[Unit] = stateRef.update(currentState =>
    currentState.copy(task :: currentState.tasks.filterNot(_.id == task.id)),
  )

object FakeTasksService:
  final case class TasksServiceState(tasks: List[Task]):
    def set(newTasks: List[Task]): TasksServiceState = copy(newTasks)

  object TasksServiceState:
    val empty: TasksServiceState = TasksServiceState(List.empty)
