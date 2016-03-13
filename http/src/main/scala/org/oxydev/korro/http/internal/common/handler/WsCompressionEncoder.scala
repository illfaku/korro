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

import org.oxydev.korro.http.internal.common.toByteBuf
import org.oxydev.korro.util.io.zipString
import org.oxydev.korro.util.log.Logging

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder
import io.netty.handler.codec.http.websocketx.{BinaryWebSocketFrame, TextWebSocketFrame, WebSocketFrame}

import java.util

import scala.util.Try

/**
 * Simple implementation of WebSocket frames compression.
 *
 * <p>Ideally it should be done using Compression Extensions for WebSocket
 * (https://tools.ietf.org/html/draft-ietf-hybi-permessage-compression-27).
 * It is implemented in Netty 5.0 but not in 4.0.
 */
@Sharable
class WsCompressionEncoder extends MessageToMessageEncoder[WebSocketFrame] with Logging {

  override def encode(ctx: ChannelHandlerContext, msg: WebSocketFrame, out: util.List[AnyRef]): Unit = msg match {
    case f: TextWebSocketFrame =>
      Try(zipString(f.text)).map(toByteBuf).map(new BinaryWebSocketFrame(_)) recover {
        case e: Throwable =>
          log.debug("Failed to compress Text WebSocket frame. Cause: {}", e.getMessage)
          f.retain()
      } foreach out.add
    case _ => out add msg.retain()
  }
}
