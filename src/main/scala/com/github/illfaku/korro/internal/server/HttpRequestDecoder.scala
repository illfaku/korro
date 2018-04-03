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
package com.github.illfaku.korro.internal.server

import com.github.illfaku.korro.dto._
import com.github.illfaku.korro.internal.common.{HttpHeadersCodec, byteBuf2bytes}

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import io.netty.handler.codec.http.FullHttpRequest

@Sharable
private[server] object HttpRequestDecoder extends MessageToMessageDecoder[FullHttpRequest] {

  override def decode(ctx: ChannelHandlerContext, msg: FullHttpRequest, out: java.util.List[AnyRef]): Unit = {

    val headers = HttpHeadersCodec.decode(msg.headers)

    out add HttpRequest(
      new HttpVersion(
        msg.protocolVersion.protocolName,
        msg.protocolVersion.majorVersion,
        msg.protocolVersion.minorVersion
      ),
      HttpRequest.Method(msg.method.name),
      HttpRequest.Uri(msg.uri),
      headers,
      HttpContent.Bytes(msg.content, headers.get(HttpHeaders.Names.ContentType).flatMap(ContentType.parse))
    )
  }
}
