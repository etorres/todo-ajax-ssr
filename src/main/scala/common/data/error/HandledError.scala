package es.eriktorr.todo
package common.data.error

import scala.util.control.NoStackTrace

@SuppressWarnings(Array("org.wartremover.warts.Null"))
abstract class HandledError(
    message: String,
    cause: Option[Throwable] = Option.empty[Throwable],
) extends NoStackTrace:
  import scala.language.unsafeNulls
  override def getCause: Throwable = cause.orNull
  override def getMessage: String = message
