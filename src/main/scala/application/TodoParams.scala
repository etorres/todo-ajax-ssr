package es.eriktorr.todo
package application

import com.monovore.decline.Opts

final case class TodoParams(verbose: Boolean)

object TodoParams:
  def opts: Opts[TodoParams] = Opts
    .flag("verbose", short = "v", help = "Print extra metadata to the logs.")
    .orFalse
    .map(TodoParams.apply)
