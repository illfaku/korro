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

import org.oxydev.korro.http.api.ws.{BinaryWsMessage, TextWsMessage, WsMessage}
import org.oxydev.korro.http.internal.common.{toByteBuf, toBytes}
import org.oxydev.korro.util.log.Logging

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageCodec
import io.netty.handler.codec.http.websocketx.{BinaryWebSocketFrame, TextWebSocketFrame, WebSocketFrame}

import java.util

@Sharable
object WsMessageCodec extends MessageToMessageCodec[WebSocketFrame, WsMessage] with Logging {

  override def encode(ctx: ChannelHandlerContext, msg: WsMessage, out: util.List[AnyRef]): Unit = msg match {
    case TextWsMessage(text) => out add new TextWebSocketFrame(text)
    case BinaryWsMessage(bytes) => out add new BinaryWebSocketFrame(bytes)
  }

  override def decode(ctx: ChannelHandlerContext, msg: WebSocketFrame, out: util.List[AnyRef]): Unit = msg match {
    case frame: BinaryWebSocketFrame => out add BinaryWsMessage(frame.content)
    case frame: TextWebSocketFrame => out add TextWsMessage(frame.text)
    case frame => log.warning("Unexpected frame: {}.", frame)
  }
}
