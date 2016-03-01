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

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object HttpStatus {

  val Ok = new HttpStatus(200, "OK")
  val BadRequest = new HttpStatus(400, "Bad Request")
  val Unauthorized = new HttpStatus(401, "Unauthorized")
  val NotFound = new HttpStatus(404, "Not Found")
  val RequestTimeout = new HttpStatus(408, "Request Timeout")
  val ServerError = new HttpStatus(500, "Internal Server Error")
  val ServiceUnavailable = new HttpStatus(503, "Service Unavailable")

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

  def apply(code: Int): HttpStatus = statusMap.getOrElse(code, new HttpStatus(code, reasonFor(code)))

  def apply(code: Int, reason: String): HttpStatus = new HttpStatus(code, reason)
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class HttpStatus(val code: Int, val reason: String) {

  def apply(content: HttpContent = HttpContent.empty, headers: HttpParams = HttpParams.empty): HttpResponse = {
    new HttpResponse(this, headers, content)
  }

  def unapply(res: HttpResponse): Option[HttpResponse] = if (this == res.status) Some(res) else None


  def canEqual(other: Any): Boolean = other.isInstanceOf[HttpStatus]

  override def equals(other: Any): Boolean = other match {
    case that: HttpStatus => (that canEqual this) && code == that.code
    case _ => false
  }

  override lazy val hashCode: Int = code.hashCode

  override lazy val toString = s"$code $reason"
}
