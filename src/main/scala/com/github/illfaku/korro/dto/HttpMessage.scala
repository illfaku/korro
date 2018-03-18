/*
 * Copyright 2018 Vladimir Konstantinov
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
package com.github.illfaku.korro.dto

import com.github.illfaku.korro.config.HttpInstruction
import com.github.illfaku.korro.dto
import com.github.illfaku.korro.dto.HttpHeaders.Names.{AcceptLanguage, Host}
import com.github.illfaku.korro.util.{Locales, QueryStringCodec}

import java.net.{MalformedURLException, URL}
import java.util.Locale

/**
 * HTTP message representation.
 */
sealed trait HttpMessage {

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
  uri: HttpRequest.Uri,
  headers: HttpParams,
  content: HttpContent
) extends HttpMessage {

  /**
   * Locale parsed from `Accept-Language` header using [[Locales#parse Locales.parse]].
   */
  implicit val locale: Locale = headers.get(AcceptLanguage).map(Locales.parse).getOrElse(Locale.getDefault)


  /**
   * Creates [[com.github.illfaku.korro.dto.HttpRequest.Outgoing HttpRequest.Outgoing]] command for HTTP client.
   * Adds `Host` header extracted from provided URL and concatenates path from it with uri from this request.
   */
  def to(url: URL, instructions: List[HttpInstruction] = Nil): HttpRequest.Outgoing = {
    val host =
      if (url.getPort == -1) url.getHost
      else url.getHost + ":" + url.getPort
    val req = copy(uri = uri.withPrefix(url.getPath), headers = headers + (Host -> host))
    new HttpRequest.Outgoing(req, url, instructions)
  }

  /**
   * @see [[to(URL)]]
   * @throws java.net.MalformedURLException If URL is malformed.
   */
  @throws(classOf[MalformedURLException])
  def to(url: String, instructions: List[HttpInstruction] = Nil): HttpRequest.Outgoing = to(new URL(url), instructions)
}

object HttpRequest {

  /**
   * Command for HTTP client created by `HttpRequest#to` methods.
   */
  class Outgoing private[korro] (val req: HttpRequest, val url: URL, val instructions: List[HttpInstruction])

  private[korro] object Outgoing {
    def unapply(out: Outgoing): Option[(HttpRequest, URL, List[HttpInstruction])] = {
      Some(out.req, out.url, out.instructions)
    }
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

    def apply(name: String): Method = new Method(name)
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
      HttpRequest(version, this, Uri(path, params), headers, content)
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

    def apply(path: String, params: HttpParams): Uri = apply(path, QueryStringCodec encode params.entries)

    def apply(path: String, query: String): Uri = {
      if (query.isEmpty) apply(path)
      else apply(s"$path?$query")
    }

    def apply(uri: String): Uri = new Uri(uri)

    def unapply(req: HttpRequest): Option[(String, HttpParams)] = Some(req.uri.path, req.uri.params)
  }

  /**
   * HTTP request URI representation.
   * @param pathWithQuery path with URL-encoded query
   */
  class Uri(val pathWithQuery: String) {

    lazy val (path: String, query: String) = {
      val pos = pathWithQuery.indexOf('?')
      if (pos == -1) (pathWithQuery, "") else (pathWithQuery.substring(0, pos), pathWithQuery.substring(pos + 1))
    }

    lazy val params = new HttpParams(QueryStringCodec decode query)

    def withPrefix(prefix: String): Uri = Uri(prefix + pathWithQuery)
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
    def unapply(req: HttpRequest): Option[String] = Some(req.uri.path)
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
    def unapplySeq(req: HttpRequest): Option[List[String]] = re.unapplySeq(req.uri.path)
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

  def Redirect(status: Status, uri: HttpRequest.Uri, headers: HttpParams = HttpParams.empty): HttpResponse = {
    status(headers = headers + (HttpHeaders.Names.Location -> uri.pathWithQuery))
  }

  object Status {

    val Ok = new Status(200, "OK")
    val MovedPermanently = new Status(301, "Moved Permanently")
    val Found = new Status(302, "Found")
    val SeeOther = new Status(303, "See Other")
    val TemporaryRedirect = new Status(307, "Temporary Redirect")
    val PermanentRedirect = new Status(308, "Permanent Redirect")
    val BadRequest = new Status(400, "Bad Request")
    val Unauthorized = new Status(401, "Unauthorized")
    val NotFound = new Status(404, "Not Found")
    val RequestTimeout = new Status(408, "Request Timeout")
    val ServerError = new Status(500, "Internal Server Error")
    val ServiceUnavailable = new Status(503, "Service Unavailable")

    private def reasonFor(code: Int): String = {
      if (code < 100) "Unknown Status"
      else if (code < 200) "Informational"
      else if (code < 300) "Successful"
      else if (code < 400) "Redirection"
      else if (code < 500) "Client Error"
      else if (code < 600) "Server Error"
      else "Unknown Status"
    }

    def apply(code: Int): Status = new Status(code, reasonFor(code))

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
