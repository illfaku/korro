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
 * Factory methods for HttpResponse class.
 *
 * @author Vladimir Konstantinov
 */
object HttpResponse {

  object Status {
    val Ok = 200
    val BadRequest = 400
    val NotFound = 404
    val InternalError = 500
  }

  def ok(content: HttpContent = EmptyHttpContent, headers: HttpHeaders = HttpHeaders.empty): HttpResponse = {
    apply(Status.Ok, content, headers)
  }

  def badRequest(content: HttpContent = EmptyHttpContent, headers: HttpHeaders = HttpHeaders.empty): HttpResponse = {
    apply(Status.BadRequest, content, headers)
  }

  def notFound(content: HttpContent = EmptyHttpContent, headers: HttpHeaders = HttpHeaders.empty): HttpResponse = {
    apply(Status.NotFound, content, headers)
  }

  def error(content: HttpContent = EmptyHttpContent, headers: HttpHeaders = HttpHeaders.empty): HttpResponse = {
    apply(Status.InternalError, content, headers)
  }
}

/**
 * HTTP response with status code, content and headers.
 *
 * @author Vladimir Konstantinov
 */
case class HttpResponse(status: Int, content: HttpContent, headers: HttpHeaders)
