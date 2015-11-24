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

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.{MessageToMessageDecoder, http => netty}

import java.nio.charset.Charset
import java.util

import scala.collection.JavaConversions._

/**
  * TODO: Add description.
  *
  * @author Vladimir Konstantinov
  */
class HttpMessageDecoder(maxContentLength: Long) extends MessageToMessageDecoder[netty.HttpObject] {

  private var message: HttpMessage = null

  private var contentType: ContentType = null

  private val byteCache = Unpooled.compositeBuffer

  override def decode(ctx: ChannelHandlerContext, msg: netty.HttpObject, out: util.List[AnyRef]): Unit = {
    if (msg.getDecoderResult.isFailure) {
      throw new IllegalStateException("Failed to decode inbound message.", msg.getDecoderResult.cause)
    } else {
      msg match {
        case m: netty.HttpMessage =>
          if (message != null) reset()
          decodeMessage(m, out)
        case m: netty.HttpContent => if (message != null) decodeContent(m, out)
        case _ => throw new IllegalStateException(s"Unknown Netty's HttpObject: ${msg.getClass}.")
      }
    }
  }

  private def decodeMessage(msg: netty.HttpMessage, out: util.List[AnyRef]): Unit = msg match {
    case m: netty.HttpRequest => decodeRequest(m, out)
    case m: netty.HttpResponse => decodeResponse(m, out)
    case _ => throw new IllegalStateException(s"Unknown Netty's HttpMessage: ${msg.getClass}.")
  }

  private def decodeRequest(msg: netty.HttpRequest, out: util.List[AnyRef]): Unit = {
    val decoder = new netty.QueryStringDecoder(msg.getUri)
    val path = decoder.path
    val params = decoder.parameters flatMap { case (name, values) => values.map(name -> _) }
    message = HttpRequest(msg.getMethod.name, path, HttpParams(params.toSeq: _*), decodeHeaders(msg.headers), HttpContent.empty)
    contentType = ContentType.parse(msg.headers.get(netty.HttpHeaders.Names.CONTENT_TYPE))
  }

  private def decodeResponse(msg: netty.HttpResponse, out: util.List[AnyRef]): Unit = {
    message = HttpResponse(msg.getStatus.code, decodeHeaders(msg.headers), HttpContent.empty)
  }

  private def decodeHeaders(headers: netty.HttpHeaders): HttpParams = {
    val result = for (header <- headers) yield header.getKey -> header.getValue
    HttpParams(result.toSeq: _*)
  }

  private def decodeContent(cnt: netty.HttpContent, out: util.List[AnyRef]): Unit = {
    byteCache.addComponent(cnt.content.retain())
    if (cnt.isInstanceOf[netty.LastHttpContent]) composeMessage(out)
  }

  private def composeMessage(out: util.List[AnyRef]): Unit = {
    if (byteCache.readableBytes > 0) {
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
    val params = decoder.parameters flatMap { case (name, values) => values.map(name -> _) }
    HttpParams(params.toSeq: _*)
  }

  private def reset(): Unit = {
    message = null
    contentType = null
    byteCache.clear()
  }
}
