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
package io.cafebabe.korro.internal.handler

import io.cafebabe.korro.api.http._
import io.cafebabe.korro.internal.ByteBufUtils.toByteBuf

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, DefaultFileRegion}
import io.netty.handler.codec.{MessageToMessageEncoder, http => netty}

import java.util

@Sharable
class HttpMessageEncoder extends MessageToMessageEncoder[HttpMessage] {

  override def encode(ctx: ChannelHandlerContext, msg: HttpMessage, out: util.List[AnyRef]): Unit = {

    val message: netty.HttpMessage = msg match {
      case req: HttpRequest =>
        new netty.DefaultHttpRequest(
          netty.HttpVersion.HTTP_1_1, netty.HttpMethod.valueOf(req.method), prepareUri(req.path, req.parameters)
        )
      case res: HttpResponse =>
        new netty.DefaultHttpResponse(
          netty.HttpVersion.HTTP_1_1, netty.HttpResponseStatus.valueOf(res.status)
        )
    }
    setHeaders(message, msg)
    out add message

    if (msg.content.length > 0) {
      out add encodeContent(msg.content)
    }
    out add netty.LastHttpContent.EMPTY_LAST_CONTENT
  }

  private def encodeContent(content: HttpContent): AnyRef = content match {
    case c: MemoryHttpContent => new netty.DefaultHttpContent(c.bytes)
    case c: FileHttpContent => new DefaultFileRegion(c.file.toFile, 0, c.length)
  }

  private def prepareUri(path: String, parameters: HttpParams): String = {
    val encoder = new netty.QueryStringEncoder(path)
    parameters.entries foreach { case (name, value) => encoder.addParam(name, value) }
    encoder.toString
  }

  private def setHeaders(nettyMsg: netty.HttpMessage, msg: HttpMessage): Unit = {
    msg.headers.entries foreach { case (name, value) => nettyMsg.headers.add(name, value) }
    if (msg.content.length > 0) {
      netty.HttpUtil.setContentLength(nettyMsg, msg.content.length)
      nettyMsg.headers.add(netty.HttpHeaderNames.CONTENT_TYPE, msg.content.contentType.toString)
    }
  }
}