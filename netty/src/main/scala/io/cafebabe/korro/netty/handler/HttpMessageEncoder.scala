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
package io.cafebabe.korro.netty.handler

import io.cafebabe.korro.api.http.HttpParams.HttpParams
import io.cafebabe.korro.api.http._
import io.cafebabe.korro.netty.ByteBufUtils.toByteBuf

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, DefaultFileRegion}
import io.netty.handler.codec.{MessageToMessageEncoder, http => netty}

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
    parameters foreach { case (name, values) => values.foreach(encoder.addParam(name, _)) }
    encoder.toString
  }

  private def setHeaders(nettyMsg: netty.HttpMessage, msg: HttpMessage): Unit = {
    msg.headers foreach { case (name, values) => values.foreach(netty.HttpHeaders.addHeader(nettyMsg, name, _)) }
    if (msg.content.length > 0) {
      netty.HttpHeaders.setContentLength(nettyMsg, msg.content.length)
      netty.HttpHeaders.addHeader(nettyMsg, netty.HttpHeaders.Names.CONTENT_TYPE, msg.content.contentType.asHeader)
    }
  }
}
