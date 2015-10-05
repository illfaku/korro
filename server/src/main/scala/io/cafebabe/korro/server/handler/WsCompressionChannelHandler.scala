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
package io.cafebabe.korro.server.handler

import io.cafebabe.korro.server.util.ByteBufUtils._
import io.cafebabe.korro.util.io.{unzipString, zipString}

import io.netty.buffer.ByteBufInputStream
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageCodec
import io.netty.handler.codec.http.websocketx.{BinaryWebSocketFrame, TextWebSocketFrame, WebSocketFrame}

import java.util

/**
 * Simple implementation of compression/decompression of WebSocket frames.
 * <br><br>
 * Ideally it should be done using Compression Extensions for WebSocket
 * (https://tools.ietf.org/html/draft-ietf-hybi-permessage-compression-27).
 * It is implemented in Netti 5.0 but not in 4.0.
 *
 * @author Vladimir Konstantinov
 */
class WsCompressionChannelHandler extends MessageToMessageCodec[WebSocketFrame, WebSocketFrame] {

  override def encode(ctx: ChannelHandlerContext, msg: WebSocketFrame, out: util.List[AnyRef]): Unit = {
    msg match {
      case f: TextWebSocketFrame => out add new BinaryWebSocketFrame(toByteBuf(zipString(f.text)))
      case _ => out add msg
    }
  }

  override def decode(ctx: ChannelHandlerContext, msg: WebSocketFrame, out: util.List[AnyRef]): Unit = {
    msg match {
      case f: BinaryWebSocketFrame => out add new TextWebSocketFrame(unzipString(new ByteBufInputStream(f.content)))
      case _ => out add msg
    }
  }
}
