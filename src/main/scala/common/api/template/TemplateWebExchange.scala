package es.eriktorr.todo
package common.api.template

import common.api.template.TemplateWebExchange.{noApplication, noSession}

import org.http4s.headers.`Accept-Language`
import org.http4s.{Request, Response}
import org.thymeleaf.web.{IWebApplication, IWebExchange, IWebRequest, IWebSession}

import java.io.InputStream
import java.security.Principal
import java.util.concurrent.atomic.AtomicReference
import java.util.function.UnaryOperator
import java.util.{Locale, Map as JavaMap, Set as JavaSet}
import scala.jdk.CollectionConverters.{MapHasAsJava, SetHasAsJava}

@SuppressWarnings(Array("org.wartremover.warts.Null"))
final class TemplateWebExchange[F[_]](request: Request[F], response: Response[F])
    extends IWebExchange:
  import scala.language.unsafeNulls

  private val attributesRef = AtomicReference[Map[String, AnyRef]](Map.empty)

  private val templateRequest = TemplateRequest(request)

  override def getRequest: IWebRequest = templateRequest

  override def getSession: IWebSession = noSession

  override def getApplication: IWebApplication = noApplication

  override def getPrincipal: Principal = new Principal:
    override def getName: String = "anonymous"

  override def getLocale: Locale =
    request.headers
      .get[`Accept-Language`]
      .map { x =>
        val lang = x.values.head
        Locale.forLanguageTag(lang.primaryTag)
      }
      .getOrElse(Locale.US)

  override def getContentType: String =
    response.contentType.map(_.mediaType.toString).getOrElse("text/html")

  override def getCharacterEncoding: String = response.charset.map(_.toString).getOrElse("UTF-8")

  override def containsAttribute(name: String): Boolean = attributesRef.get().contains(name)

  override def getAttributeCount: Int = attributesRef.get().size

  override def getAllAttributeNames: JavaSet[String] = attributesRef.get().keySet.asJava

  override def getAttributeMap: JavaMap[String, AnyRef] = attributesRef.get().asJava

  override def getAttributeValue(name: String): AnyRef = attributesRef.get().getOrElse(name, null)

  override def setAttributeValue(name: String, value: AnyRef): Unit =
    attributesRef.getAndUpdate((currentState: Map[String, AnyRef]) =>
      currentState + (name -> value),
    )

  override def removeAttribute(name: String): Unit =
    attributesRef.getAndUpdate((currentState: Map[String, AnyRef]) => currentState - name)

  override def transformURL(url: String): String = url

object TemplateWebExchange:
  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  private val noApplication = new IWebApplication:
    import scala.language.unsafeNulls

    override def containsAttribute(name: String): Boolean = false

    override def getAttributeCount: Int = 0

    override def getAllAttributeNames: JavaSet[String] = JavaSet.of()

    override def getAttributeMap: JavaMap[String, AnyRef] = JavaMap.of()

    override def getAttributeValue(name: String): AnyRef = null

    override def setAttributeValue(name: String, value: AnyRef): Unit = ()

    override def removeAttribute(name: String): Unit = ()

    override def resourceExists(path: String): Boolean = false

    override def getResourceAsStream(path: String): InputStream = null

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  private val noSession = new IWebSession:
    import scala.language.unsafeNulls

    override def exists(): Boolean = false

    override def containsAttribute(name: String): Boolean = false

    override def getAttributeCount: Int = 0

    override def getAllAttributeNames: JavaSet[String] = JavaSet.of()

    override def getAttributeMap: JavaMap[String, AnyRef] = JavaMap.of()

    override def getAttributeValue(name: String): AnyRef = null

    override def setAttributeValue(name: String, value: AnyRef): Unit = ()

    override def removeAttribute(name: String): Unit = ()
