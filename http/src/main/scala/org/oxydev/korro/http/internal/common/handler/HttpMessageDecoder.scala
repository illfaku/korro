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

import org.oxydev.korro.http.api.ContentType.DefaultCharset
import org.oxydev.korro.http.api.HttpRequest.Method
import org.oxydev.korro.http.api._
import org.oxydev.korro.http.internal.common.ByteBufUtils.toBytes
import org.oxydev.korro.http.internal.common.ChannelFutureExt
import org.oxydev.korro.util.log.Logging

import io.netty.buffer.{CompositeByteBuf, Unpooled}
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.{MessageToMessageDecoder, http => netty}

import java.util

import scala.collection.JavaConversions._

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class HttpMessageDecoder(maxSize: Long) extends MessageToMessageDecoder[netty.HttpObject] with Logging {

  private lazy val BadRequest = new netty.DefaultFullHttpResponse(
    netty.HttpVersion.HTTP_1_1, netty.HttpResponseStatus.BAD_REQUEST
  )

  private lazy val TooBigContent = new netty.DefaultFullHttpResponse(
    netty.HttpVersion.HTTP_1_1, netty.HttpResponseStatus.BAD_REQUEST,
    Unpooled.copiedBuffer(s"Content length is too big. Limit is $maxSize bytes.", DefaultCharset)
  )


  private var message: HttpMessage = null

  private var contentType: ContentType = null

  private var byteCache: CompositeByteBuf = null


  override def handlerAdded(ctx: ChannelHandlerContext): Unit = byteCache = Unpooled.compositeBuffer

  override def handlerRemoved(ctx: ChannelHandlerContext): Unit = byteCache.release()


  override def decode(ctx: ChannelHandlerContext, msg: netty.HttpObject, out: util.List[AnyRef]): Unit = {
    if (msg.getDecoderResult.isFailure) {
      finish(ctx, BadRequest)
      log.error(msg.getDecoderResult.cause, "Failed to decode inbound message.")
    } else {
      msg match {
        case m: netty.HttpMessage =>
          if (message != null) reset()
          val size = netty.HttpHeaders.getContentLength(m, 0)
          if (size > maxSize) finish(ctx, TooBigContent) else decodeMessage(m, out)
        case m: netty.HttpContent => if (message != null) decodeContent(ctx, m, out)
      }
    }
  }

  private def decodeMessage(msg: netty.HttpMessage, out: util.List[AnyRef]): Unit = msg match {
    case m: netty.HttpRequest => decodeRequest(m)
    case m: netty.HttpResponse => decodeResponse(m)
  }

  private def decodeRequest(msg: netty.HttpRequest): Unit = {
    message = HttpRequest(Method(msg.getMethod.name), msg.getUri, decodeHeaders(msg.headers), HttpContent.empty)
    contentType = parseContentType(msg)
  }

  private def decodeResponse(msg: netty.HttpResponse): Unit = {
    val status = HttpStatus(msg.getStatus.code, msg.getStatus.reasonPhrase)
    message = HttpResponse(status, decodeHeaders(msg.headers), HttpContent.empty)
    contentType = parseContentType(msg)
  }

  private def parseContentType(msg: netty.HttpMessage): ContentType = {
    ContentType.parse(msg.headers.get(netty.HttpHeaders.Names.CONTENT_TYPE))
  }

  private def decodeHeaders(headers: netty.HttpHeaders): HttpParams = {
    val result = for (header <- headers) yield header.getKey -> header.getValue
    HttpParams(result.toSeq: _*)
  }

  private def decodeContent(ctx: ChannelHandlerContext, cnt: netty.HttpContent, out: util.List[AnyRef]): Unit = {
    if (byteCache.readableBytes + cnt.content.readableBytes > maxSize) {
      finish(ctx, TooBigContent)
      reset()
    } else {
      byteCache.addComponent(cnt.content.retain())
      byteCache.writerIndex(byteCache.writerIndex() + cnt.content.readableBytes)
      if (cnt.isInstanceOf[netty.LastHttpContent]) composeMessage(out)
    }
  }

  private def composeMessage(out: util.List[AnyRef]): Unit = {
    if (byteCache.isReadable) {
      message = message match {
        case m: HttpRequest => m.copy(content = HttpContent.memory(byteCache, contentType))
        case m: HttpResponse => m.copy(content = HttpContent.memory(byteCache, contentType))
      }
    }
    out add message
    reset()
  }

  private def reset(): Unit = {
    message = null
    contentType = null
    byteCache.clear()
  }

  private def finish(ctx: ChannelHandlerContext, msg: netty.FullHttpResponse): Unit = {
    ctx.writeAndFlush(msg.retain()).closeChannel()
  }
}
