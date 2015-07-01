/*
 * Copyright (C) 2015  Vladimir Konstantinov, Yuriy Gintsyak
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
package io.cafebabe.http.server.api

/**
 * HTTP request methods as constructors and handy extractors of request.
 * <br><br>
 * Extractors usage:
 * {{{
 *   import HttpMethod._
 *   req match {
 *     case Get(path, params, content, headers) => ???
 *     case Post(path, params, content, headers) => ???
 *   }
 * }}}
 *
 * @author Vladimir Konstantinov
 */
object HttpMethod {

  case object Get extends HttpMethod("GET")

  case object Post extends HttpMethod("POST")

  case object Put extends HttpMethod("PUT")

  case object Delete extends HttpMethod("DELETE")

  case object Head extends HttpMethod("HEAD")

  case object Connect extends HttpMethod("CONNECT")

  case object Options extends HttpMethod("OPTIONS")

  case object Trace extends HttpMethod("TRACE")

  def forName(name: String): HttpMethod = name.toUpperCase match {
    case "GET" => Get
    case "POST" => Post
    case "PUT" => Put
    case "DELETE" => Delete
    case "HEAD" => Head
    case "CONNECT" => Connect
    case "OPTIONS" => Options
    case "TRACE" => Trace
  }
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
sealed abstract class HttpMethod(val name: String) {

  def apply(
    path: String,
    parameters: QueryParams = QueryParams.empty,
    content: HttpContent = EmptyHttpContent,
    headers: HttpHeaders = HttpHeaders.empty
  ): HttpRequest = {
    HttpRequest(name, path, parameters, content, headers)
  }

  def unapply(req: HttpRequest): Option[(String, QueryParams, HttpContent, HttpHeaders)] =
    if (name == req.method.toUpperCase) Some(req.path, req.parameters, req.content, req.headers) else None
}
