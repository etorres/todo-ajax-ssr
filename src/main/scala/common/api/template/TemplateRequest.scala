package es.eriktorr.todo
package common.api.template

import org.http4s.Request
import org.thymeleaf.web.IWebRequest
import org.typelevel.ci.CIString

import java.util.{Map as JavaMap, Set as JavaSet}
import scala.jdk.CollectionConverters.{MapHasAsJava, SetHasAsJava}

final class TemplateRequest[F[_]](request: Request[F]) extends IWebRequest:

  override def getMethod: String = request.method.name

  override def getScheme: String = request.uri.scheme.map(_.value).getOrElse("https")

  override def getServerName: String = request.serverAddr.map(_.toString).getOrElse("localhost")

  override def getServerPort: Integer = request.serverPort.map(_.value).getOrElse(80)

  override def getApplicationPath: String = ""

  override def getPathWithinApplication: String = request.pathInfo.toString

  override def getQueryString: String = request.queryString

  override def containsHeader(name: String): Boolean =
    request.headers.headers.exists(_.name == CIString(name))

  override def getHeaderCount: Int = request.headers.headers.length

  override def getAllHeaderNames: JavaSet[String] =
    request.headers.headers.map(_.name.toString).toSet.asJava

  override def getHeaderMap: JavaMap[String, Array[String]] =
    request.headers.headers
      .map(header => header.name.toString -> header.value)
      .groupBy { case (name, _) =>
        name
      }
      .view
      .mapValues(_.map { case (_, value) => value }.toArray)
      .toMap
      .asJava

  override def getHeaderValues(name: String): Array[String] =
    request.headers.headers.filter(_.name == CIString(name)).map(_.value).toArray

  override def containsParameter(name: String): Boolean = request.params.contains(name)

  override def getParameterCount: Int = request.params.size

  override def getAllParameterNames: JavaSet[String] = request.params.keySet.asJava

  override def getParameterMap: JavaMap[String, Array[String]] =
    request.params.map { case (name, value) => name -> Array(value) }.asJava

  override def getParameterValues(name: String): Array[String] =
    request.params.get(name).map(Array(_)).getOrElse(Array.empty[String])

  override def containsCookie(name: String): Boolean = request.cookies.exists(_.name == name)

  override def getCookieCount: Int = request.cookies.length

  override def getAllCookieNames: JavaSet[String] = request.cookies.map(_.name).toSet.asJava

  override def getCookieMap: JavaMap[String, Array[String]] =
    request.cookies
      .map(cookie => cookie.name -> cookie.content)
      .groupBy { case (name, _) => name }
      .view
      .mapValues(_.map { case (_, value) => value }.toArray)
      .toMap
      .asJava

  override def getCookieValues(name: String): Array[String] =
    request.cookies.filter(_.name == name).map(_.content).toArray
