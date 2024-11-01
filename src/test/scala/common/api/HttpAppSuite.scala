package es.eriktorr.todo
package common.api

import common.api.HttpAppSuiteRunner.HttpAppState

import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.http4s.Status

trait HttpAppSuite extends CatsEffectSuite with ScalaCheckEffectSuite

object HttpAppSuite:
  final case class TestCase[A, B](
      initialState: HttpAppState,
      expectedState: HttpAppState,
      request: A,
      expectedResponse: (B, Status),
  )
