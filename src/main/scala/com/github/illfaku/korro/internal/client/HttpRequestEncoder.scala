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

import com.github.illfaku.korro.dto.HttpRequest
import com.github.illfaku.korro.internal.common.{HttpContentCodec, HttpHeadersCodec}

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder
import io.netty.handler.codec.http.{DefaultHttpRequest, HttpMethod, HttpVersion}

@Sharable
private[client] object HttpRequestEncoder extends MessageToMessageEncoder[HttpRequest] {

  override def encode(ctx: ChannelHandlerContext, msg: HttpRequest, out: java.util.List[AnyRef]): Unit = {

    out add new DefaultHttpRequest(
      HttpVersion.valueOf(msg.version.toString),
      HttpMethod.valueOf(msg.method.name),
      msg.uri.toString,
      HttpHeadersCodec.encode(msg.headers, msg.content)
    )

    HttpContentCodec.encode(msg.content).foreach(out.add)
  }
}
