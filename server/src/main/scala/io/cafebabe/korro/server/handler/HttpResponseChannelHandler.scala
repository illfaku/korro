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
package io.cafebabe.korro.server.handler

import io.cafebabe.korro.api.http.HttpResponse
import io.cafebabe.korro.netty.{FileStreamNettyContent, DefaultNettyContent}
import io.cafebabe.korro.netty.convert.{HttpContentConverter, HttpHeadersConverter, HttpResponseConverter}

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{DefaultFileRegion, ChannelPromise, ChannelOutboundHandlerAdapter, ChannelHandlerContext}
import io.netty.handler.codec.MessageToMessageEncoder
import io.netty.handler.codec.http._

import java.util

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
@Sharable
class HttpResponseChannelHandler extends MessageToMessageEncoder[HttpResponse] {

  override def encode(ctx: ChannelHandlerContext, msg: HttpResponse, out: util.List[AnyRef]): Unit = {
    val status = HttpResponseStatus.valueOf(msg.status)
    val nettyHeaders = HttpHeadersConverter.toNetty(msg.headers)

    HttpContentConverter.toNetty(msg.content) match {

      case content: DefaultNettyContent =>

        val response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content.data)
        response.headers.add(nettyHeaders)
        response.headers.add(HttpHeaders.Names.CONTENT_LENGTH, content.contentLength)
        response.headers.add(HttpHeaders.Names.CONTENT_TYPE, content.contentType)

        out add response

      case content: FileStreamNettyContent =>

        val response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status)
        response.headers.add(nettyHeaders)
        response.headers.add(HttpHeaders.Names.CONTENT_LENGTH, content.contentLength)
        response.headers.add(HttpHeaders.Names.CONTENT_TYPE, content.contentType)

        out add response
        out add content.toFileRegion
        out add LastHttpContent.EMPTY_LAST_CONTENT
    }
  }
}
