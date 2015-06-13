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
package io.cafebabe.http.impl.util

import akka.actor.ActorNotFound
import akka.pattern.AskTimeoutException
import io.cafebabe.http.api.HttpResponse
import io.cafebabe.http.api.exception.{BadRequestException, NotFoundException}
import io.cafebabe.http.impl.util.ByteBufUtils.toByteBuf
import io.netty.handler.codec.http.{HttpResponse => _, _}
import org.slf4j.LoggerFactory

import java.util.UUID

/**
 * @author Vladimir Konstantinov
 * @version 1.0 (4/12/2015)
 */
object ResponseUtils {

  type Headers = Map[String, String]

  private val log = LoggerFactory.getLogger(getClass)

  def toNettyResponse(res: HttpResponse): FullHttpResponse = {
    newNettyResponse(HttpResponseStatus.valueOf(res.status), res.content, res.headers)
  }

  val toErrorResponse: PartialFunction[Throwable, FullHttpResponse] = {
    case e: ActorNotFound => newNettyResponse(HttpResponseStatus.SERVICE_UNAVAILABLE, "Service unavailable.")
    case e: AskTimeoutException => newNettyResponse(HttpResponseStatus.REQUEST_TIMEOUT, "Request timeout.")
    case e: NotFoundException => newNettyResponse(HttpResponseStatus.NOT_FOUND, "Not found.")
    case e: BadRequestException => newNettyResponse(HttpResponseStatus.BAD_REQUEST, "Bad request.")
    case e: Throwable =>
      val message = s"Internal Error #${UUID.randomUUID}."
      log.error(message, e)
      newNettyResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, message)
  }

  private def newNettyResponse(status: HttpResponseStatus, content: String): FullHttpResponse = {
    newNettyResponse(status, content, Map.empty)
  }

  private def newNettyResponse(status: HttpResponseStatus, content: String, headers: Headers): FullHttpResponse = {

    val result = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, toByteBuf(content.getBytes))

    def setIfAbsent(key: String, value: String): Unit = if (!headers.contains(key)) result.headers.set(key, value)

    setIfAbsent(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=utf-8")
    setIfAbsent(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE)
    setIfAbsent(HttpHeaders.Names.CONTENT_LENGTH, content.length.toString)
    headers foreach {
      case (key, value) => result.headers.set(key, value)
    }
    result
  }
}
