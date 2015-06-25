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
package io.cafebabe.http.server.impl.util

import akka.actor.ActorNotFound
import akka.pattern.AskTimeoutException
import io.cafebabe.http.server.api.{HttpContent, HttpHeaders, HttpResponse, _}
import io.cafebabe.http.server.api.exception.{BadRequestException, NotFoundException}
import io.cafebabe.http.server.impl.HttpHeadersConverter
import io.cafebabe.http.server.impl.util.ByteBufUtils._
import io.netty.handler.codec.http.HttpHeaders.{Names => HeaderNames, Values => HeaderValues}
import io.netty.handler.codec.http._
import org.slf4j.LoggerFactory

import java.util.UUID

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object ResponseUtils {

  private val log = LoggerFactory.getLogger(getClass)

  def toNettyResponse(res: HttpResponse): FullHttpResponse = {
    nettyResponse(HttpResponseStatus.valueOf(res.status), res.headers, res.content)
  }

  val toErrorResponse: PartialFunction[Throwable, FullHttpResponse] = {
    case e: ActorNotFound => simpleNettyResponse(HttpResponseStatus.SERVICE_UNAVAILABLE, "Service unavailable.")
    case e: AskTimeoutException => simpleNettyResponse(HttpResponseStatus.REQUEST_TIMEOUT, "Request timeout.")
    case e: NotFoundException => simpleNettyResponse(HttpResponseStatus.NOT_FOUND, "Not found.")
    case e: BadRequestException => simpleNettyResponse(HttpResponseStatus.BAD_REQUEST, "Bad request.")
    case e: Throwable =>
      val message = s"Internal Error #${UUID.randomUUID}."
      log.error(message, e)
      simpleNettyResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, message)
  }

  private def simpleNettyResponse(status: HttpResponseStatus, content: String): FullHttpResponse = {
    nettyResponse(status, HttpHeaders.empty, TextHttpContent(content))
  }

  private def nettyResponse(status: HttpResponseStatus, headers: HttpHeaders, content: HttpContent): FullHttpResponse = {

    val nettyHeaders = HttpHeadersConverter.toNetty(headers)
    val buf = content match {
      case TextHttpContent(text) =>
        nettyHeaders.add(HeaderNames.CONTENT_TYPE, "text/plain; charset=utf-8")
        toByteBuf(text)
      case JsonHttpContent(json) =>
        nettyHeaders.add(HeaderNames.CONTENT_TYPE, "application/json; charset=utf-8")
        toByteBuf(StringUtils.toString(json))
      case EmptyHttpContent => emptyByteBuf
    }
    nettyHeaders.add(HeaderNames.CONTENT_LENGTH, buf.readableBytes)
    nettyHeaders.add(HeaderNames.CONNECTION, HeaderValues.CLOSE)

    val result = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, buf)
    result.headers.add(nettyHeaders)
    result
  }
}
