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
 * HTTP request methods as objects to be contained in request and handy extractors of request.
 * <br><br>
 * Extractors usage:
 * {{{
 *   import HttpMethod._
 *   req match {
 *     case GET(path, params, content, headers) => ???
 *     case POST(path, params, content, headers) => ???
 *   }
 * }}}
 *
 * @author Vladimir Konstantinov
 */
object HttpMethod {

  case object GET extends HttpMethod("GET")

  case object POST extends HttpMethod("POST")

  case object PUT extends HttpMethod("PUT")

  case object DELETE extends HttpMethod("DELETE")

  case object HEAD extends HttpMethod("HEAD")

  case object CONNECT extends HttpMethod("CONNECT")

  case object OPTIONS extends HttpMethod("OPTIONS")

  case object TRACE extends HttpMethod("TRACE")

  def forName(name: String): HttpMethod = name.toUpperCase match {
    case "GET" => GET
    case "POST" => POST
    case "PUT" => PUT
    case "DELETE" => DELETE
    case "HEAD" => HEAD
    case "CONNECT" => CONNECT
    case "OPTIONS" => OPTIONS
    case "TRACE" => TRACE
  }
}

sealed abstract class HttpMethod(val name: String) {

  def apply(
    path: String,
    parameters: QueryParams = QueryParams.empty,
    content: HttpContent = EmptyHttpContent,
    headers: HttpHeaders = HttpHeaders.empty
  ): HttpRequest = {
    HttpRequest(this, path, parameters, content, headers)
  }

  def unapply(req: HttpRequest): Option[(String, QueryParams, HttpContent, HttpHeaders)] =
    if (this == req.method) Some(req.path, req.parameters, req.content, req.headers) else None
}
