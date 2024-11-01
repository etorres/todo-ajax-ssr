package es.eriktorr.todo
package common.api.template

import org.http4s.{Request, Response}
import org.thymeleaf.context.{AbstractContext, IWebContext}
import org.thymeleaf.web.IWebExchange

import scala.jdk.CollectionConverters.MapHasAsJava

final class TemplateContext[F[_]](request: Request[F], response: Response[F])
    extends AbstractContext
    with IWebContext:
  override def getExchange: IWebExchange = TemplateWebExchange(request, response)

object TemplateContext:
  def from[F[_]](
      request: Request[F],
      response: Response[F],
      variable: (String, AnyRef),
  ): TemplateContext[F] = from(request, response, Map(variable))

  private def from[F[_]](
      request: Request[F],
      response: Response[F],
      variables: Map[String, AnyRef],
  ): TemplateContext[F] =
    val templateContext = TemplateContext[F](request, response)
    templateContext.setVariables(variables.asJava)
    templateContext
