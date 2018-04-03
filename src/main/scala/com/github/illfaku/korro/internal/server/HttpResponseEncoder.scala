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

import com.github.illfaku.korro.dto.HttpResponse
import com.github.illfaku.korro.internal.common.{HttpContentCodec, HttpHeadersCodec}

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder
import io.netty.handler.codec.http.{DefaultHttpResponse, HttpResponseStatus, HttpVersion}

@Sharable
private[server] object HttpResponseEncoder extends MessageToMessageEncoder[HttpResponse] {

  override def encode(ctx: ChannelHandlerContext, msg: HttpResponse, out: java.util.List[AnyRef]): Unit = {

    out add new DefaultHttpResponse(
      HttpVersion.valueOf(msg.version.toString),
      HttpResponseStatus.valueOf(msg.status.code, msg.status.reason),
      HttpHeadersCodec.encode(msg.headers, msg.content)
    )

    HttpContentCodec.encode(msg.content).foreach(out.add)
  }
}
