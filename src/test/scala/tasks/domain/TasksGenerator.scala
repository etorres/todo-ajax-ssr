package es.eriktorr.todo
package tasks.domain

import spec.StringGenerators.alphaNumericStringBetween
import tasks.domain.Task.{Id, Label}

import org.scalacheck.Gen

object TasksGenerator:
  val idGen: Gen[Id] = Gen.choose(0, 10000).map(Id.applyUnsafe)

  private val labelGen = alphaNumericStringBetween(10, 20).map(Label.applyUnsafe)

  def taskGen(idGen: Gen[Id] = idGen): Gen[Task] = for
    id <- idGen
    label <- labelGen
    completed <- Gen.oneOf(true, false)
  yield Task(id, label, completed)
