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
package org.oxydev.korro.api.http

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object HttpStatus {
  object Ok extends HttpStatus(200)
  object BadRequest extends HttpStatus(400)
  object NotFound extends HttpStatus(404)
  object RequestTimeout extends HttpStatus(408)
  object ServerError extends HttpStatus(500)
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
sealed abstract class HttpStatus(val code: Int) {

  def apply(content: HttpContent = HttpContent.empty, headers: HttpParams = HttpParams.empty): HttpResponse = {
    new HttpResponse(code, headers, content)
  }

  def unapply(res: HttpResponse): Option[HttpResponse] = {
    if (code == res.status) Some(res)
    else None
  }
}
