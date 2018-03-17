/*
 * Copyright 2016-2017 Vladimir Konstantinov, Yuriy Gintsyak
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
package com.github.illfaku.korro.api

import com.github.illfaku.korro.api
import com.github.illfaku.korro.util.i18n.Locales
import com.github.illfaku.korro.util.net.QueryStringCodec

import java.net.{MalformedURLException, URL}
import java.util.Locale

sealed trait HttpObject

/**
 * HTTP message representation.
 */
sealed trait HttpMessage extends HttpObject {

  /**
   * HTTP version.
   */
  val version: HttpVersion

  /**
   * HTTP headers.
   */
  val headers: HttpParams

  /**
   * HTTP message body.
   */
  val content: HttpContent
}

/**
 * HTTP request representation.
 */
case class HttpRequest(
  version: HttpVersion,
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
   * Locale parsed from `Accept-Language` header using [[com.github.illfaku.korro.util.i18n.Locales#parse Locales.parse]].
   */
  implicit val locale: Locale = headers.get("Accept-Language").map(Locales.parse).getOrElse(Locale.getDefault)


  /**
   * Creates [[api.HttpRequest.Outgoing HttpRequest.Outgoing]] command for HTTP client.
   * Adds Host header extracted from provided URL and concatenates path from it with uri from this request.
   */
  def to(url: URL): HttpRequest.Outgoing = {
    val host =
      if (url.getPort == -1) url.getHost
      else url.getHost + ":" + url.getPort
    val req = copy(uri = url.getPath + uri, headers = headers + ("Host" -> host))
    new HttpRequest.Outgoing(req, url)
  }

  /**
   * @see [[to(URL)]]
   * @throws java.net.MalformedURLException If URL is malformed.
   */
  @throws(classOf[MalformedURLException])
  def to(url: String): HttpRequest.Outgoing = to(new URL(url))
}

object HttpRequest {

  /**
   * Command for HTTP client created by `HttpRequest#to` methods.
   */
  class Outgoing private [http] (val req: HttpRequest, val url: URL)

  private [http] object Outgoing {
    def unapply(out: Outgoing): Option[(HttpRequest, URL)] = Some(out.req, out.url)
  }

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
      params: HttpParams = HttpParams.empty,
      content: HttpContent = HttpContent.empty,
      headers: HttpParams = HttpParams.empty,
      version: HttpVersion = HttpVersion.Http11
    ): HttpRequest = {
      val query = if (params.isEmpty) "" else "?" + QueryStringCodec.encode(params.entries)
      HttpRequest(version, this, path + query, headers, content)
    }

    def unapply(req: HttpRequest): Option[HttpRequest] = if (this == req.method) Some(req) else None


    override def equals(other: Any): Boolean = other match {
      case that: Method => name == that.name
      case _ => false
    }

    override lazy val hashCode = name.hashCode

    override val toString = name
  }


  object Uri {

    def apply(path: String, params: HttpParams): Uri = {
      Option(params).filter(_.isEmpty)
      val query = if (params.isEmpty) "" else QueryStringCodec.encode(params.entries)
      Uri(path, Some(query))
    }
  }

  /**
   * HTTP request URI representation.
   * @param path path
   * @param query URL-encoded query
   */
  case class Uri(path: String, query: Option[String]) {
    lazy val params = new HttpParams(QueryStringCodec.decode(query))
  }

  /**
   * Extracts path from HttpRequest.
   * {{{
   *   val req: HttpRequest = ...
   *   req match {
   *     case Path("/some/path") => ...
   *   }
   * }}}
   */
  object Path {
    def unapply(req: HttpRequest): Option[String] = Some(req.path)
  }

  /**
   * Extracts segments of a path.
   * {{{
   *   Get("/a/b/c/d/e") match {
   *     case Path("/a/b" / x / "d" / y) => x + "-" + y   // c-e
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

  /**
   * Matches path of HttpRequest against a pattern and extracts matching groups from it.
   * {{{
   *   val req: HttpRequest = ...
   *   val ApiRegex = new PathRegex("/api/(\\d\\.\\d)/.*")
   *   req match {
   *     case ApiRegex("1.0") => ...
   *   }
   * }}}
   *
   * @param pattern Path prefix to test against HttpRequest.
   */
  class PathRegex(pattern: String) {
    private val re = pattern.r
    def unapplySeq(req: HttpRequest): Option[List[String]] = re.unapplySeq(req.path)
  }

  /**
   * String interpolation for paths. It allows to extract path's segments.
   * {{{
   *   import HttpRequest.PathInterpolation
   *   Get("/a/b/c/d/e") match {
   *     case path"/a/b/$x/d/$y" => x + "-" + y   // c-e
   *   }
   * }}}
   */
  implicit class PathInterpolation(sc: StringContext) {
    def path = new PathRegex(sc.parts.mkString("([^/]+)"))
  }
}

/**
 * HTTP response representation.
 */
case class HttpResponse(
  version: HttpVersion,
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

    def apply(
      content: HttpContent = HttpContent.empty,
      headers: HttpParams = HttpParams.empty,
      version: HttpVersion = HttpVersion.Http11
    ): HttpResponse = {
      HttpResponse(version, this, headers, content)
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

case class HttpChunk(id: Int, bytes: Array[Byte]) extends HttpObject
