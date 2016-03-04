/*
 * Copyright (C) 2015, 2016  Vladimir Konstantinov, Yuriy Gintsyak
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.oxydev.korro.http.api

import org.oxydev.korro.util.i18n.Locales
import org.oxydev.korro.util.protocol.http.QueryStringCodec

import java.util.Locale

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
sealed trait HttpMessage {
  def headers: HttpParams
  def content: HttpContent
}

/**
 * HTTP request representation.
 *
 * @author Vladimir Konstantinov
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

  lazy val locale: Locale = headers.get("Accept-Language").map(Locales.parse).getOrElse(Locale.getDefault)
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
   *
   * @author Vladimir Konstantinov
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
   * TODO: Add description.
   *
   * @author Vladimir Konstantinov
   */
  class Method(val name: String) {

    def apply(
      path: String,
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
}

/**
 * HTTP response representation.
 *
 * @author Vladimir Konstantinov
 */
case class HttpResponse(status: HttpStatus, headers: HttpParams, content: HttpContent) extends HttpMessage
