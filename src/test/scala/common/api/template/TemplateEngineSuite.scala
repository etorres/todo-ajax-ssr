package es.eriktorr.todo
package common.api.template

import common.api.template.TemplateEngineSuite.testCaseGen
import spec.CollectionGenerators.nDistinct
import spec.GenExtensions.genExtensions
import tasks.api.TaskResponse
import tasks.domain.Task
import tasks.domain.TasksGenerator.{idGen, taskGen}

import cats.effect.IO
import cats.implicits.toTraverseOps
import munit.CatsEffectSuite
import org.http4s.implicits.uri
import org.http4s.{Method, Request, Response}
import org.scalacheck.Gen
import org.scalacheck.cats.implicits.genInstances

import scala.jdk.CollectionConverters.SeqHasAsJava

final class TemplateEngineSuite extends CatsEffectSuite:
  test("should process a template"):
    val testCase = testCaseGen.sampleWithSeed()
    val templateEngine = TemplateEngine.html[IO]()
    val context = TemplateContext[IO](
      request = Request[IO](method = Method.GET, uri = uri"/todo/tasks"),
      response = Response[IO](),
    )
    context.setVariable("todos", testCase.tasks.map(TaskResponse.fromTask).asJava)
    templateEngine.htmlFrom(context, "fake-list").assertEquals(testCase.expected)

object TemplateEngineSuite:
  final private case class TestCase(tasks: List[Task], expected: String)

  private val testCaseGen = for
    size <- Gen.choose(1, 3)
    ids <- nDistinct(size, idGen)
    tasks <- ids.traverse(id => taskGen(id))
    expected = tasks.map(trFrom).mkString("")
  yield TestCase(tasks, expected)

  private def trFrom(task: Task) =
    s"""<tr>
       |    <td>${task.id}</td>
       |    <td>${task.label}</td>
       |    <td>${task.completed}</td>
       |</tr>""".stripMargin
