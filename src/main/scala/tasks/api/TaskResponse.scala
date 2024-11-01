package es.eriktorr.todo
package tasks.api

import tasks.domain.Task

@SuppressWarnings(Array("org.wartremover.warts.Var"))
final class TaskResponse(var id: Int, var label: String, var completed: Boolean):

  def this() = this(0, "", false)

  def getId: Int = id

  def setId(id: Int): Unit = this.id = id

  def getLabel: String = label

  def setLabel(label: String): Unit = this.label = label

  def getCompleted: Boolean = completed

  def setCompleted(completed: Boolean): Unit = this.completed = completed

  override def toString: String = s"TaskResponse: ($id, $label, $completed)"

object TaskResponse:
  def fromTask(task: Task): TaskResponse = TaskResponse(task.id, task.label, task.completed)
