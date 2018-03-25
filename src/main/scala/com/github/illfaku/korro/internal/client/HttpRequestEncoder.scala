/*
 * Copyright 2018 Vladimir Konstantinov
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
package com.github.illfaku.korro.internal.client

import com.github.illfaku.korro.dto._
import com.github.illfaku.korro.internal.common.bytes2byteBuf

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, DefaultFileRegion}
import io.netty.handler.codec.http.DefaultHttpHeaders
import io.netty.handler.codec.{MessageToMessageEncoder, http => netty}

import java.io.File

@Sharable
private[client] object HttpRequestEncoder extends MessageToMessageEncoder[HttpRequest] {

  override def encode(ctx: ChannelHandlerContext, req: HttpRequest, out: java.util.List[AnyRef]): Unit = {

    val headers = new DefaultHttpHeaders()
    req.headers.entries foreach { case (name, value) => headers.add(name, value) }

    if (req.content.contentLength > 0) {
      if (!headers.contains(netty.HttpHeaderNames.CONTENT_LENGTH)) {
        headers.set(netty.HttpHeaderNames.CONTENT_LENGTH, req.content.contentLength)
      }
      req.content.contentType foreach { t =>
        if (!headers.contains(netty.HttpHeaderNames.CONTENT_TYPE)) {
          headers.add(netty.HttpHeaderNames.CONTENT_TYPE, t.toString)
        }
      }
    }

   out add new netty.DefaultHttpRequest(
      netty.HttpVersion.valueOf(req.version.toString),
      netty.HttpMethod.valueOf(req.method.name),
      req.uri.toString,
      headers
    )

    req.content match {

      case BytesHttpContent(bytes, _) if bytes.nonEmpty =>
        out add new netty.DefaultLastHttpContent(bytes)

      case FileHttpContent(path, size, _) if size > 0 =>
        out add new DefaultFileRegion(new File(path), 0, size)
        out add netty.LastHttpContent.EMPTY_LAST_CONTENT

      case _ =>
        out add netty.LastHttpContent.EMPTY_LAST_CONTENT
    }
  }
}
