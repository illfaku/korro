/*
 * Copyright 2016 Vladimir Konstantinov, Yuriy Gintsyak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.oxydev.korro.http.internal.common.handler

import org.oxydev.korro.http.api.ContentType.DefaultCharset
import org.oxydev.korro.http.api.HttpRequest.Method
import org.oxydev.korro.http.api._
import org.oxydev.korro.http.internal.common.{ChannelFutureExt, toBytes}
import org.oxydev.korro.util.log.Logging

import io.netty.buffer.{CompositeByteBuf, Unpooled}
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.{MessageToMessageDecoder, http => netty}

import java.util

import scala.collection.JavaConversions._

/**
 * Aggregates Netty's HttpObjects (HttpMessage and HttpContents up to LastHttpContent) to Korro's HttpMessage.
 *
 * @param maxSize Maximum size of content.
 */
class HttpMessageDecoder(maxSize: Long) extends MessageToMessageDecoder[netty.HttpObject] with Logging {

  private lazy val BadRequest = new netty.DefaultFullHttpResponse(
    netty.HttpVersion.HTTP_1_1, netty.HttpResponseStatus.BAD_REQUEST
  )

  private lazy val TooBigContent = new netty.DefaultFullHttpResponse(
    netty.HttpVersion.HTTP_1_1, netty.HttpResponseStatus.BAD_REQUEST,
    Unpooled.copiedBuffer(s"Content length is too big. Limit is $maxSize bytes.", DefaultCharset)
  )


  private var message: HttpMessage = _

  private var byteCache: CompositeByteBuf = _


  override def decode(ctx: ChannelHandlerContext, msg: netty.HttpObject, out: util.List[AnyRef]): Unit = {
    if (msg.decoderResult.isFailure) {
      finish(ctx, BadRequest)
      log.debug("Failed to decode inbound message. {}", msg.decoderResult.cause)
    } else {
      msg match {
        case m: netty.HttpMessage =>
          reset()
          if (netty.HttpUtil.getContentLength(m, 0) > maxSize) finish(ctx, TooBigContent)
          else decodeMessage(m, out)
        case m: netty.HttpContent => if (message != null) decodeContent(ctx, m, out)
      }
    }
  }

  private def decodeMessage(msg: netty.HttpMessage, out: util.List[AnyRef]): Unit = msg match {
    case m: netty.HttpRequest => decodeRequest(m)
    case m: netty.HttpResponse => decodeResponse(m)
  }

  private def decodeRequest(msg: netty.HttpRequest): Unit = {
    message = HttpRequest(Method(msg.method.name), msg.uri, decodeHeaders(msg.headers), HttpContent.empty)
  }

  private def decodeResponse(msg: netty.HttpResponse): Unit = {
    val status = HttpResponse.Status(msg.status.code, msg.status.reasonPhrase)
    message = HttpResponse(status, decodeHeaders(msg.headers), HttpContent.empty)
  }

  private def decodeHeaders(headers: netty.HttpHeaders): HttpParams = {
    val result = for (header <- headers) yield header.getKey -> header.getValue
    HttpParams(result.toSeq: _*)
  }

  private def decodeContent(ctx: ChannelHandlerContext, cnt: netty.HttpContent, out: util.List[AnyRef]): Unit = {
    val size = if (byteCache == null) cnt.content.readableBytes else byteCache.readableBytes + cnt.content.readableBytes
    if (size > maxSize) {
      finish(ctx, TooBigContent)
    } else {
      if (cnt.content.isReadable) {
        if (byteCache == null) byteCache = ctx.alloc.compositeBuffer
        byteCache.addComponent(cnt.content.retain())
        byteCache.writerIndex(byteCache.writerIndex() + cnt.content.readableBytes)
      }
      if (cnt.isInstanceOf[netty.LastHttpContent]) composeMessage(out)
    }
  }

  private def composeMessage(out: util.List[AnyRef]): Unit = {
    if (byteCache != null) {
      val contentType = message.headers.get("Content-Type").flatMap(ContentType.parse)
      message = message match {
        case m: HttpRequest => m.copy(content = HttpContent.memory(byteCache, contentType))
        case m: HttpResponse => m.copy(content = HttpContent.memory(byteCache, contentType))
      }
    }
    out add message
    reset()
  }


  @inline private def reset(): Unit = {
    message = null
    if (byteCache != null) {
      byteCache.release()
      byteCache = null
    }
  }

  @inline private def finish(ctx: ChannelHandlerContext, msg: netty.FullHttpResponse): Unit = {
    ctx.writeAndFlush(msg.retain()).closeChannel()
    reset()
  }


  override def handlerRemoved(ctx: ChannelHandlerContext): Unit = reset()

  override def channelInactive(ctx: ChannelHandlerContext): Unit = {
    reset()
    super.channelInactive(ctx)
  }
}
