ThisBuild / organization := "es.eriktorr"
ThisBuild / version := "1.0.0"
ThisBuild / idePackagePrefix := Some("es.eriktorr.todo")
Global / excludeLintKeys += idePackagePrefix

ThisBuild / scalaVersion := "3.3.4"

ThisBuild / scalacOptions ++= Seq(
  "-Xfatal-warnings",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-source:future", // https://github.com/oleg-py/better-monadic-for
  "-Yexplicit-nulls", // https://docs.scala-lang.org/scala3/reference/other-new-features/explicit-nulls.html
  "-Ysafe-init", // https://docs.scala-lang.org/scala3/reference/other-new-features/safe-initialization.html
  "-Wnonunit-statement",
  "-Wunused:all",
)

Global / cancelable := true
Global / fork := true
Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / semanticdbEnabled := true
ThisBuild / javacOptions ++= Seq("-source", "21", "-target", "21")
ThisBuild / scalafixDependencies += "org.typelevel" %% "typelevel-scalafix" % "0.3.1"

lazy val MUnitFramework = new TestFramework("munit.Framework")
lazy val warts = Warts.unsafe.filter(_ != Wart.DefaultArguments)

Compile / doc / sources := Seq()
Compile / compile / wartremoverErrors ++= warts
Test / compile / wartremoverErrors ++= warts
Test / testFrameworks += MUnitFramework
Test / testOptions += Tests.Argument(MUnitFramework, "--exclude-tags=online")

Test / envVars := Map(
  "SBT_TEST_ENV_VARS" -> "true",
  "TODO_HEALTH_LIVENESS_PATH" -> "/liveness-path",
  "TODO_HEALTH_READINESS_PATH" -> "/readiness-path",
  "TODO_HTTP_HOST" -> "localhost",
  "TODO_HTTP_MAX_ACTIVE_REQUESTS" -> "1024",
  "TODO_HTTP_PORT" -> "8000",
  "TODO_HTTP_TIMEOUT" -> "11s",
)

addCommandAlias(
  "check",
  "; undeclaredCompileDependenciesTest; unusedCompileDependenciesTest; scalafixAll; scalafmtSbtCheck; scalafmtCheckAll",
)

lazy val root = (project in file("."))
  .settings(
    name := "todo-ssr",
    Universal / maintainer := "https://eriktorr.es",
    Compile / mainClass := Some("es.eriktorr.todo.App"),
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-core" % "3.11.0",
      "co.fs2" %% "fs2-io" % "3.11.0",
      "com.47deg" %% "scalacheck-toolbox-datetime" % "0.7.0" % Test,
      "com.comcast" %% "ip4s-core" % "3.6.0",
      "com.lmax" % "disruptor" % "3.4.4" % Runtime,
      "com.monovore" %% "decline" % "2.4.1",
      "com.monovore" %% "decline-effect" % "2.4.1",
      "io.chrisdavenport" %% "cats-scalacheck" % "0.3.2" % Test,
      "io.github.iltotore" %% "iron" % "2.6.0",
      "io.github.iltotore" %% "iron-decline" % "2.6.0",
      "io.prometheus" % "simpleclient" % "0.16.0",
      "org.apache.logging.log4j" % "log4j-core" % "2.24.1" % Runtime,
      "org.apache.logging.log4j" % "log4j-layout-template-json" % "2.24.1" % Runtime,
      "org.apache.logging.log4j" % "log4j-slf4j2-impl" % "2.24.1" % Runtime,
      "org.http4s" %% "http4s-core" % "0.23.29",
      "org.http4s" %% "http4s-dsl" % "0.23.29",
      "org.http4s" %% "http4s-ember-server" % "0.23.29",
      "org.http4s" %% "http4s-prometheus-metrics" % "0.25.0",
      "org.http4s" %% "http4s-server" % "0.23.29",
      "org.scalameta" %% "munit" % "1.0.0" % Test,
      "org.scalameta" %% "munit-scalacheck" % "1.0.0" % Test,
      "org.thymeleaf" % "thymeleaf" % "3.1.2.RELEASE",
      "org.thymeleaf.testing" % "thymeleaf-testing" % "3.1.2.RELEASE" % Test,
      "org.typelevel" %% "case-insensitive" % "1.4.2",
      "org.typelevel" %% "cats-core" % "2.12.0",
      "org.typelevel" %% "cats-effect" % "3.5.4",
      "org.typelevel" %% "cats-effect-kernel" % "3.5.4",
      "org.typelevel" %% "cats-kernel" % "2.12.0",
      "org.typelevel" %% "log4cats-core" % "2.7.0",
      "org.typelevel" %% "log4cats-slf4j" % "2.7.0",
      "org.typelevel" %% "munit-cats-effect" % "2.0.0" % Test,
      "org.typelevel" %% "scalacheck-effect" % "1.0.4" % Test,
      "org.typelevel" %% "scalacheck-effect-munit" % "1.0.4" % Test,
      "org.typelevel" %% "vault" % "3.6.0",
      "org.webjars" % "bootstrap" % "5.3.3" % Runtime,
      "org.webjars" % "webjars-locator" % "0.52" % Runtime,
      "org.webjars.npm" % "htmx.org" % "2.0.3" % Runtime,
    ),
    onLoadMessage := {
      s"""Custom tasks:
         |check - run all project checks
         |""".stripMargin
    },
  )
  .enablePlugins(JavaAppPackaging)
