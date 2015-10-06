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
package io.cafebabe.korro.server.convert

import io.cafebabe.korro.api.http.HttpParams.HttpParams
import io.cafebabe.korro.api.http.{EmptyHttpContent, HttpContent, HttpResponse, TextHttpContent}

import akka.actor.ActorNotFound
import akka.pattern.AskTimeoutException
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

  def fromNetty(response: FullHttpResponse): HttpResponse = {
    HttpResponse(
      response.getStatus.code,
      HttpContentConverter.fromNetty(response.content, response.headers),
      HttpHeadersConverter.fromNetty(response.headers)
    )
  }

  def toNetty(response: HttpResponse): FullHttpResponse = {
    nettyResponse(HttpResponseStatus.valueOf(response.status), response.content, response.headers)
  }

  val toError: PartialFunction[Throwable, FullHttpResponse] = {
    case e: ActorNotFound => nettyResponse(SERVICE_UNAVAILABLE)
    case e: AskTimeoutException => nettyResponse(REQUEST_TIMEOUT)
    case e: IllegalArgumentException => nettyResponse(BAD_REQUEST, TextHttpContent(e.getMessage))
    case e: Throwable =>
      val message = s"Internal Error #${UUID.randomUUID}."
      log.error(message, e)
      nettyResponse(INTERNAL_SERVER_ERROR, TextHttpContent(message))
  }

  private def nettyResponse(status: HttpResponseStatus): FullHttpResponse = nettyResponse(status, EmptyHttpContent)

  private def nettyResponse(status: HttpResponseStatus, content: HttpContent): FullHttpResponse = {
    nettyResponse(status, content, Map.empty)
  }

  private def nettyResponse(status: HttpResponseStatus, content: HttpContent, headers: HttpParams): FullHttpResponse = {
    val nettyHeaders = HttpHeadersConverter.toNetty(headers)
    val (buf, contentHeaders) = HttpContentConverter.toNetty(content)
    val result = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, buf)
    result.headers.add(nettyHeaders).add(contentHeaders)
    result
  }
}
