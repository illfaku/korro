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

import io.cafebabe.korro.api.http.ContentType.DefaultCharset
import io.cafebabe.korro.api.http.ContentType.Names.FormUrlEncoded
import io.cafebabe.korro.api.http._
import io.cafebabe.korro.internal.ByteBufUtils.toBytes
import io.cafebabe.korro.util.log.Logging

import io.netty.buffer.{CompositeByteBuf, Unpooled}
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext}
import io.netty.handler.codec.{MessageToMessageDecoder, http => netty}

import java.nio.charset.Charset
import java.util

import scala.collection.JavaConverters._
import scala.util.Try

class HttpMessageDecoder(maxSize: Long) extends MessageToMessageDecoder[netty.HttpObject] with Logging {

  private val BadRequest = new netty.DefaultFullHttpResponse(
    netty.HttpVersion.HTTP_1_1, netty.HttpResponseStatus.BAD_REQUEST
  )

  private val TooBigContent = new netty.DefaultFullHttpResponse(
    netty.HttpVersion.HTTP_1_1, netty.HttpResponseStatus.BAD_REQUEST,
    Unpooled.copiedBuffer(s"Content length is too big. Limit is $maxSize bytes.", DefaultCharset)
  )


  private var message: HttpMessage = null

  private var contentType: ContentType = null

  private var byteCache: CompositeByteBuf = null


  override def handlerAdded(ctx: ChannelHandlerContext): Unit = byteCache = Unpooled.compositeBuffer

  override def handlerRemoved(ctx: ChannelHandlerContext): Unit = byteCache.release()


  override def decode(ctx: ChannelHandlerContext, msg: netty.HttpObject, out: util.List[AnyRef]): Unit = {
    if (msg.decoderResult.isFailure) {
      ctx.writeAndFlush(BadRequest.retain()).addListener(ChannelFutureListener.CLOSE)
      log.error(msg.decoderResult.cause, "Failed to decode inbound message.")
    } else msg match {

      case m: netty.HttpMessage =>
        if (message != null) reset()
        val size = getContentLength(m)
        if (size > maxSize) ctx.writeAndFlush(TooBigContent.retain()).addListener(ChannelFutureListener.CLOSE)
        else decodeMessage(m, out)

      case m: netty.HttpContent => if (message != null) decodeContent(ctx, m, out)

      case _ => throw new IllegalStateException(s"Unknown Netty's HttpObject: ${msg.getClass}.")
    }
  }

  private def decodeMessage(msg: netty.HttpMessage, out: util.List[AnyRef]): Unit = msg match {
    case m: netty.HttpRequest => decodeRequest(m)
    case m: netty.HttpResponse => decodeResponse(m)
    case _ => throw new IllegalStateException(s"Unknown Netty's HttpMessage: ${msg.getClass}.")
  }

  private def decodeRequest(msg: netty.HttpRequest): Unit = {
    val decoder = new netty.QueryStringDecoder(msg.uri)
    val path = decoder.path
    val params = decoder.parameters.asScala flatMap { case (name, values) => values.asScala.map(name -> _) }
    message = HttpRequest(
      msg.method.name, path, HttpParams(params.toSeq: _*), decodeHeaders(msg.headers), HttpContent.empty
    )
    contentType = ContentType.parse(msg.headers.get(netty.HttpHeaders.Names.CONTENT_TYPE))
  }

  private def decodeResponse(msg: netty.HttpResponse): Unit = {
    message = HttpResponse(msg.status.code, decodeHeaders(msg.headers), HttpContent.empty)
    contentType = ContentType.parse(msg.headers.get(netty.HttpHeaders.Names.CONTENT_TYPE))
  }

  private def decodeHeaders(headers: netty.HttpHeaders): HttpParams = {
    val result = for (header <- headers.asScala) yield header.getKey -> header.getValue
    HttpParams(result.toSeq: _*)
  }

  private def decodeContent(ctx: ChannelHandlerContext, cnt: netty.HttpContent, out: util.List[AnyRef]): Unit = {
    if (byteCache.readableBytes + cnt.content.readableBytes > maxSize) {
      ctx.writeAndFlush(TooBigContent.retain()).addListener(ChannelFutureListener.CLOSE)
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
        case m: HttpRequest =>
          contentType match {
            case ContentType(FormUrlEncoded, charset) =>
              m.copy(parameters = m.parameters ++ decodeParametersFromBody(charset.getOrElse(DefaultCharset)))
            case _ => m.copy(content = HttpContent.memory(byteCache, contentType))
          }
        case m: HttpResponse => m.copy(content = HttpContent.memory(byteCache, contentType))
      }
    }
    out add message
    reset()
  }

  private def decodeParametersFromBody(charset: Charset): HttpParams = {
    val decoder = new netty.QueryStringDecoder(byteCache.toString(charset), false)
    val params = decoder.parameters.asScala flatMap { case (name, values) => values.asScala.map(name -> _) }
    HttpParams(params.toSeq: _*)
  }

  private def getContentLength(msg: netty.HttpMessage): Long = {
    Option(msg.headers.get(netty.HttpHeaders.Names.CONTENT_LENGTH)).flatMap(l => Try(l.toLong).toOption).getOrElse(0L)
  }

  private def reset(): Unit = {
    message = null
    contentType = null
    byteCache.clear()
  }
}
