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
package org.oxydev.korro.http.internal.common.handler

import org.oxydev.korro.http.api._
import org.oxydev.korro.http.internal.common.ByteBufUtils.toByteBuf

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, DefaultFileRegion}
import io.netty.handler.codec.{MessageToMessageEncoder, http => netty}

import java.io.File
import java.util

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
@Sharable
class HttpMessageEncoder extends MessageToMessageEncoder[HttpMessage] {

  override def encode(ctx: ChannelHandlerContext, msg: HttpMessage, out: util.List[AnyRef]): Unit = {

    val message: netty.HttpMessage = msg match {
      case req: HttpRequest =>
        new netty.DefaultHttpRequest(
          netty.HttpVersion.HTTP_1_1, netty.HttpMethod.valueOf(req.method.name), req.uri
        )
      case res: HttpResponse =>
        new netty.DefaultHttpResponse(
          netty.HttpVersion.HTTP_1_1, new netty.HttpResponseStatus(res.status.code, res.status.reason)
        )
    }
    setHeaders(message, msg)
    out add message

    if (msg.content.length > 0) out add encodeContent(msg.content)

    out add netty.LastHttpContent.EMPTY_LAST_CONTENT
  }

  private def encodeContent(content: HttpContent): AnyRef = content match {
    case c: MemoryHttpContent => new netty.DefaultHttpContent(c.bytes)
    case c: FileHttpContent => new DefaultFileRegion(new File(c.path), 0, c.length)
  }

  private def setHeaders(message: netty.HttpMessage, msg: HttpMessage): Unit = {
    msg.headers.entries foreach { case (name, value) => netty.HttpHeaders.addHeader(message, name, value) }
    if (msg.content.length > 0) {
      netty.HttpHeaders.setContentLength(message, msg.content.length)
      netty.HttpHeaders.addHeader(message, netty.HttpHeaders.Names.CONTENT_TYPE, msg.content.contentType.toString)
    }
  }
}
