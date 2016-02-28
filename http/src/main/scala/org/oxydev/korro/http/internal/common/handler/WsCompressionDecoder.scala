/*
 * Copyright (C) 2015, 2016  Vladimir Konstantinov, Yuriy Gintsyak
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
package org.oxydev.korro.http.internal.common.handler

import org.oxydev.korro.util.io.unzipString
import org.oxydev.korro.util.log.Logging

import io.netty.buffer.ByteBufInputStream
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import io.netty.handler.codec.http.websocketx.{BinaryWebSocketFrame, TextWebSocketFrame, WebSocketFrame}

import java.util

import scala.util.Try

/**
 * Simple implementation of WebSocket frames decompression.
 *
 * <p>Ideally it should be done using Compression Extensions for WebSocket
 * (https://tools.ietf.org/html/draft-ietf-hybi-permessage-compression-27).
 * It is implemented in Netty 5.0 but not in 4.0.
 *
 * @author Vladimir Konstantinov
 */
@Sharable
class WsCompressionDecoder extends MessageToMessageDecoder[WebSocketFrame] with Logging {

  override def decode(ctx: ChannelHandlerContext, msg: WebSocketFrame, out: util.List[AnyRef]): Unit = msg match {
    case f: BinaryWebSocketFrame =>
      Try(unzipString(new ByteBufInputStream(f.content))).map(new TextWebSocketFrame(_)) recover {
        case e: Throwable =>
          log.debug("Failed to decompress Binary WebSocket frame. Cause: {}", e.getMessage)
          f.retain()
      } foreach out.add
    case _ => out add msg.retain()
  }
}
