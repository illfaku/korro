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

import org.oxydev.korro.util.protocol.http.QueryStringUtils

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
case class HttpRequest(method: HttpMethod, uri: String, headers: HttpParams, content: HttpContent) extends HttpMessage {

  lazy val (path, queryString) = QueryStringUtils.split(uri)

  lazy val parameters: HttpParams = new HttpParams(QueryStringUtils.decode(queryString))
}

/**
 * HTTP response representation.
 *
 * @author Vladimir Konstantinov
 */
case class HttpResponse(status: HttpStatus, headers: HttpParams, content: HttpContent) extends HttpMessage
