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

import io.cafebabe.korro.api.ws._
import io.cafebabe.korro.internal.ByteBufUtils.{toByteBuf, toBytes}
import io.cafebabe.korro.util.log.Logging

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageCodec
import io.netty.handler.codec.http.websocketx._

@Sharable
class WsMessageCodec extends MessageToMessageCodec[WebSocketFrame, WsMessage] with Logging {

  override def encode(ctx: ChannelHandlerContext, msg: WsMessage, out: java.util.List[AnyRef]): Unit = msg match {
    case _: ConnectWsMessage =>
      log.warning("Unable to convert ConnectWsMessage to any WebSocketFrame.")
      out add Unpooled.EMPTY_BUFFER
    case DisconnectWsMessage(code, reason) => out add new CloseWebSocketFrame(code, reason.orNull)
    case PingWsMessage => out add new PingWebSocketFrame
    case PongWsMessage => out add new PongWebSocketFrame
    case TextWsMessage(text) => out add new TextWebSocketFrame(text)
    case BinaryWsMessage(bytes) => out add new BinaryWebSocketFrame(bytes)
  }

  override def decode(ctx: ChannelHandlerContext, msg: WebSocketFrame, out: java.util.List[AnyRef]): Unit = msg match {
    case _: CloseWebSocketFrame => out add DisconnectWsMessage
    case _: PingWebSocketFrame => out add PingWsMessage
    case _: PongWebSocketFrame => out add PongWsMessage
    case frame: BinaryWebSocketFrame => out add BinaryWsMessage(frame.content)
    case frame: TextWebSocketFrame => out add TextWsMessage(frame.text)
    case frame => log.warning("Unknown frame: {}.", frame)
  }
}
