package es.eriktorr.todo
package common.api.template

import cats.effect.Sync
import org.thymeleaf.TemplateEngine as ThymeleafTemplateEngine
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

import scala.concurrent.duration.{DurationInt, FiniteDuration}

final class TemplateEngine[F[_]] private (templateEngine: ThymeleafTemplateEngine)(using
    F: Sync[F],
):
  def htmlFrom(context: TemplateContext[F], name: String): F[String] =
    F.blocking(templateEngine.process(name, context).nn)

object TemplateEngine:
  def html[F[_]](
      prefix: String = "/WEB-INF/templates/",
      suffix: String = ".html",
      cacheExpiresIn: FiniteDuration = 1.hour,
  )(using
      F: Sync[F],
  ): TemplateEngine[F] =
    val templateResolver = ClassLoaderTemplateResolver()
    templateResolver.setTemplateMode(TemplateMode.HTML)
    templateResolver.setPrefix(prefix)
    templateResolver.setSuffix(suffix)
    templateResolver.setCacheTTLMs(cacheExpiresIn.toMillis)
    templateResolver.setCacheable(true)

    val templateEngine = ThymeleafTemplateEngine()
    templateEngine.setTemplateResolver(templateResolver)

    TemplateEngine(templateEngine)
