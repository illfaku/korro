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
package com.github.illfaku.korro.internal.common.handler

import com.github.illfaku.korro.api._
import org.oxydev.korro.api._
import org.oxydev.korro.internal.common.toByteBuf
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, DefaultFileRegion}
import io.netty.handler.codec.{MessageToMessageEncoder, http => netty}

import java.nio.file.{Files, Paths}
import java.util

/**
 * Converts Korro's HttpMessage to Netty's HttpObjects (message, content and last content).
 */
@Sharable
object HttpMessageEncoder extends MessageToMessageEncoder[HttpMessage] {

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

    val content = msg.content match {

      case c: MemoryHttpContent if c.length > 0 =>
        netty.HttpUtil.setContentLength(message, c.length)
        setContentType(message, msg)
        Some(new netty.DefaultHttpContent(c.bytes))

      case c: FileHttpContent =>
        val path = Paths.get(c.path)
        val size = Files.size(path)
        netty.HttpUtil.setContentLength(message, size)
        setContentType(message, msg)
        Some(new DefaultFileRegion(path.toFile, 0, size))

      case _ => None
    }

    out add message
    content.foreach(out.add)
    out add netty.LastHttpContent.EMPTY_LAST_CONTENT
  }

  private def setContentType(message: netty.HttpMessage, msg: HttpMessage): Unit = {
    msg.content.contentType foreach { t => message.headers.add(netty.HttpHeaderNames.CONTENT_TYPE, t.toString) }
  }

  private def setHeaders(message: netty.HttpMessage, msg: HttpMessage): Unit = {
    msg.headers.entries foreach { case (name, value) => message.headers.add(name, value) }
  }
}
