package es.eriktorr.todo
package tasks.domain

import common.data.refined.Constraints.NonEmptyString
import tasks.domain.Task.{Id, Label}

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.numeric.Positive0

final case class Task(id: Id, label: Label, completed: Boolean = false)

object Task:
  opaque type Id <: Int :| Positive0 = Int :| Positive0

  object Id extends RefinedTypeOps[Int, Positive0, Id]:
    def eitherString(value: String): Either[String, Id] = value.toIntOption match
      case Some(number) => Id.either(number)
      case None => Left(s"Expected a number but got got: $value")

  opaque type Label <: String :| NonEmptyString = String :| NonEmptyString

  object Label extends RefinedTypeOps[String, NonEmptyString, Label]
