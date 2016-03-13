/*
 * Copyright 2016 Vladimir Konstantinov, Yuriy Gintsyak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.oxydev.korro.http.api

import org.oxydev.korro.util.i18n.Locales
import org.oxydev.korro.util.protocol.http.QueryStringCodec

import java.net.{MalformedURLException, URL}
import java.util.Locale

/**
 * HTTP message representation.
 */
sealed trait HttpMessage {

  /**
   * HTTP headers.
   */
  def headers: HttpParams

  /**
   * Message body.
   */
  def content: HttpContent
}

/**
 * HTTP request representation.
 */
case class HttpRequest(
  method: HttpRequest.Method,
  uri: String,
  headers: HttpParams,
  content: HttpContent
) extends HttpMessage {

  lazy val (path: String, queryString: String) = {
    val pos = uri.indexOf('?')
    if (pos == -1) (uri, "") else (uri.substring(0, pos), uri.substring(pos + 1))
  }

  lazy val parameters: HttpParams = new HttpParams(QueryStringCodec.decode(queryString))

  /**
   * Locale parsed from Accept-Language header using [[Locales#parse]].
   */
  implicit val locale: Locale = headers.get("Accept-Language").map(Locales.parse).getOrElse(Locale.getDefault)

  /**
   * Creates [[OutgoingHttpRequest]] for HTTP client. Adds Host header extracted from provided URL and
   * if [[path]] is empty in this request then also extracts it from URL and sets it.
   */
  def to(url: URL): OutgoingHttpRequest = {
    val req =
      if (url.getPath != "" && (uri == "" || uri.startsWith("?"))) this.copy(uri = url.getPath + uri)
      else this
    val host =
      if (url.getPort == -1) url.getHost
      else url.getHost + ":" + url.getPort
    OutgoingHttpRequest(req.copy(headers = req.headers + ("Host" -> host)), url)
  }

  /**
   * @see [[to(URL)]]
   * @throws java.net.MalformedURLException If URL is malformed.
   */
  @throws(classOf[MalformedURLException])
  def to(url: String): OutgoingHttpRequest = to(new URL(url))
}

object HttpRequest {

  /**
   * HTTP request methods as constructors and handy extractors of request.
   * <br><br>
   * Extractors usage:
   * {{{
   *   val req: HttpRequest = ???
   *   req match {
   *     case Get(r) => ???
   *     case Post(r) => ???
   *   }
   * }}}
   */
  object Method {

    val Get = new Method("GET")
    val Post = new Method("POST")
    val Put = new Method("PUT")
    val Delete = new Method("DELETE")
    val Head = new Method("HEAD")
    val Connect = new Method("CONNECT")
    val Options = new Method("OPTIONS")
    val Trace = new Method("TRACE")

    private val methodMap = Map(
      Get.name -> Get,
      Post.name -> Post,
      Put.name -> Put,
      Delete.name -> Delete,
      Head.name -> Head,
      Connect.name -> Connect,
      Options.name -> Options,
      Trace.name -> Trace
    )

    def apply(name: String): Method = {
      require(name != null, "name is null")
      require(name.trim.nonEmpty, "name is empty")
      methodMap.getOrElse(name, new Method(name))
    }
  }

  /**
   * HTTP request method representation.
   */
  class Method(val name: String) {

    def apply(
      path: String = "",
      parameters: HttpParams = HttpParams.empty,
      content: HttpContent = HttpContent.empty,
      headers: HttpParams = HttpParams.empty
    ): HttpRequest = {
      val query = if (parameters.isEmpty) "" else "?" + QueryStringCodec.encode(parameters.entries)
      new HttpRequest(this, s"$path$query", headers, content)
    }

    def unapply(req: HttpRequest): Option[HttpRequest] = if (this == req.method) Some(req) else None


    override def equals(other: Any): Boolean = other match {
      case that: Method => name == that.name
      case _ => false
    }

    override lazy val hashCode = name.hashCode

    override val toString = name
  }

  /**
   * Extracts path from HttpRequest.
   */
  object Path {
    def unapply(req: HttpRequest): Option[String] = Some(req.path)
  }

  /**
   * Extracts segments of a path.
   * {{{
   *   "/a/b/c/d/e" match {
   *     case "/a/b" / v1 / "d" / v2 => v1 + "-" + v2   // c-e
   *   }
   * }}}
   */
  object / {
    def unapply(path: String): Option[(String, String)] = {
      val pos = path.lastIndexOf('/')
      if (pos >= 0) Some(path.substring(0, pos) -> path.substring(pos + 1))
      else None
    }
  }
}

/**
 * HTTP response representation.
 */
case class HttpResponse(
  status: HttpResponse.Status,
  headers: HttpParams,
  content: HttpContent
) extends HttpMessage

object HttpResponse {

  object Status {

    val Ok = new Status(200, "OK")
    val BadRequest = new Status(400, "Bad Request")
    val Unauthorized = new Status(401, "Unauthorized")
    val NotFound = new Status(404, "Not Found")
    val RequestTimeout = new Status(408, "Request Timeout")
    val ServerError = new Status(500, "Internal Server Error")
    val ServiceUnavailable = new Status(503, "Service Unavailable")

    private val statusMap = Map(
      Ok.code -> Ok,
      BadRequest.code -> BadRequest,
      Unauthorized.code -> Unauthorized,
      NotFound.code -> NotFound,
      RequestTimeout.code -> RequestTimeout,
      ServerError.code -> ServerError,
      ServiceUnavailable.code -> ServiceUnavailable
    )

    private def reasonFor(code: Int): String = {
      if (code < 100) "Unknown Status"
      else if (code < 200) "Informational"
      else if (code < 300) "Successful"
      else if (code < 400) "Redirection"
      else if (code < 500) "Client Error"
      else if (code < 600) "Server Error"
      else "Unknown Status"
    }

    def apply(code: Int): Status = statusMap.getOrElse(code, new Status(code, reasonFor(code)))

    def apply(code: Int, reason: String): Status = new Status(code, reason)
  }

  /**
   * HTTP response status representation.
   */
  class Status(val code: Int, val reason: String) {

    def apply(content: HttpContent = HttpContent.empty, headers: HttpParams = HttpParams.empty): HttpResponse = {
      new HttpResponse(this, headers, content)
    }

    def unapply(res: HttpResponse): Option[HttpResponse] = if (this == res.status) Some(res) else None


    override def equals(other: Any): Boolean = other match {
      case that: Status => code == that.code
      case _ => false
    }

    override val hashCode: Int = code

    override lazy val toString = s"$code $reason"
  }
}
