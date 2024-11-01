package es.eriktorr.todo
package tasks.api

import common.api.HttpAppSuite
import common.api.HttpAppSuite.TestCase
import common.api.HttpAppSuiteRunner.{runWith, HttpAppState}
import common.api.template.{TemplateContext, TemplateEngine}
import spec.CollectionGenerators.nDistinct
import tasks.api.TasksWebControllerSuite.testCaseGen
import tasks.domain.Task
import tasks.domain.TasksGenerator.{idGen, taskGen}

import cats.effect.IO
import cats.implicits.toTraverseOps
import org.http4s.implicits.uri
import org.http4s.{Method, Request, Response, Status}
import org.scalacheck.Gen
import org.scalacheck.cats.implicits.genInstances
import org.scalacheck.effect.PropF.forAllF

import scala.jdk.CollectionConverters.SeqHasAsJava

final class TasksWebControllerSuite extends HttpAppSuite:
  test("should list all tasks"):
    forAllF(testCaseGen): testCase =>
      (for
        (result, finalState) <- runWith[String](
          testCase.initialState,
          Request(method = Method.GET, uri = uri"/todo/tasks"),
        )
        ((templateEngine, context), expectedStatus) = testCase.expectedResponse
        expectedBody <- templateEngine.htmlFrom(context, "index")
        expected = Right((expectedBody, expectedStatus))
      yield (result, finalState, expected)).map { case (result, finalState, expected) =>
        assertEquals(finalState, testCase.expectedState)
        assertEquals(result, expected)
      }

object TasksWebControllerSuite:
  private val testCaseGen = for
    size <- Gen.choose(3, 7)
    ids <- nDistinct(size, idGen)
    tasks <- ids.traverse(id => taskGen(id))
    initialState = HttpAppState.empty.setTasks(tasks)
    expectedState = initialState.copy()
    (templateEngine, context) = contextFrom(tasks)
  yield TestCase(initialState, expectedState, (), ((templateEngine, context), Status.Ok))

  private def contextFrom(tasks: List[Task]) =
    val templateEngine = TemplateEngine.html[IO]()
    val context = TemplateContext[IO](
      request = Request[IO](method = Method.GET, uri = uri"/todo/tasks"),
      response = Response[IO](),
    )
    context.setVariable("todos", tasks.map(TaskResponse.fromTask).asJava)
    (templateEngine, context)
