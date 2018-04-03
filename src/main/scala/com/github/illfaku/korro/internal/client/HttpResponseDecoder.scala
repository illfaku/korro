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
import com.github.illfaku.korro.internal.common.{HttpContentCodec, HttpHeadersCodec}

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import io.netty.handler.codec.http.FullHttpResponse

@Sharable
private[client] object HttpResponseDecoder extends MessageToMessageDecoder[FullHttpResponse] {

  override def decode(ctx: ChannelHandlerContext, msg: FullHttpResponse, out: java.util.List[AnyRef]): Unit = {
    if (msg.decoderResult.isFailure) {
      ctx.fireExceptionCaught(msg.decoderResult.cause)
    } else {
      out add decodeResponse(msg)
    }
  }

  private def decodeResponse(msg: FullHttpResponse): HttpResponse = {
    HttpResponse(
      new HttpVersion(
        msg.protocolVersion.protocolName,
        msg.protocolVersion.majorVersion,
        msg.protocolVersion.minorVersion
      ),
      HttpResponse.Status(msg.status.code, msg.status.reasonPhrase),
      HttpHeadersCodec.decode(msg.headers),
      HttpContentCodec.decode(msg.content, msg.headers)
    )
  }
}
