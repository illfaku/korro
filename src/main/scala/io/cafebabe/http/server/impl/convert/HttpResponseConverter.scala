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
package io.cafebabe.http.server.impl.convert

import akka.actor.ActorNotFound
import akka.pattern.AskTimeoutException
import io.cafebabe.http.server.api.exception.{BadRequestException, NotFoundException}
import io.cafebabe.http.server.api.{HttpContent, HttpHeaders, HttpResponse, _}
import io.netty.handler.codec.http.HttpResponseStatus._
import io.netty.handler.codec.http._
import org.slf4j.LoggerFactory

import java.util.UUID

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object HttpResponseConverter {

  private val log = LoggerFactory.getLogger(getClass)

  def toNetty(res: HttpResponse): FullHttpResponse = {
    nettyResponse(HttpResponseStatus.valueOf(res.status), res.content, res.headers)
  }

  val toError: PartialFunction[Throwable, FullHttpResponse] = {
    case e: ActorNotFound => nettyResponse(SERVICE_UNAVAILABLE)
    case e: AskTimeoutException => nettyResponse(REQUEST_TIMEOUT)
    case e: NotFoundException => nettyResponse(NOT_FOUND)
    case e: BadRequestException => nettyResponse(BAD_REQUEST, TextHttpContent(e.getMessage))
    case e: Throwable =>
      val message = s"Internal Error #${UUID.randomUUID}."
      log.error(message, e)
      nettyResponse(INTERNAL_SERVER_ERROR, TextHttpContent(message))
  }

  private def nettyResponse(status: HttpResponseStatus): FullHttpResponse = nettyResponse(status, EmptyHttpContent)

  private def nettyResponse(status: HttpResponseStatus, content: HttpContent): FullHttpResponse = {
    nettyResponse(status, content, HttpHeaders.empty)
  }

  private def nettyResponse(status: HttpResponseStatus, content: HttpContent, headers: HttpHeaders): FullHttpResponse = {
    val nettyHeaders = HttpHeadersConverter.toNetty(headers)
    val (buf, contentHeaders) = HttpContentConverter.toNetty(content)
    val result = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, buf)
    result.headers.add(nettyHeaders).add(contentHeaders)
    result
  }
}
