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
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object HttpStatus {

  case object Ok extends HttpStatus(200)

  case object BadRequest extends HttpStatus(400)

  case object NotFound extends HttpStatus(404)

  case object ServerError extends HttpStatus(500)

  def forCode(code: Int): HttpStatus = code match {
    case 200 => Ok
    case 400 => BadRequest
    case 404 => NotFound
    case 500 => ServerError
  }
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
sealed abstract class HttpStatus(val code: Int) {

  def apply(content: HttpContent = EmptyHttpContent, headers: HttpHeaders = HttpHeaders.empty): HttpResponse = {
    HttpResponse(code, content, headers)
  }

  def unapply(res: HttpResponse): Option[(HttpContent, HttpHeaders)] = {
    if (code == res.status) Some(res.content, res.headers) else None
  }
}
