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
package com.github.illfaku.korro.internal.common

import com.github.illfaku.korro.dto.ws._

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageCodec
import io.netty.handler.codec.http.websocketx._

@Sharable
private[internal] object WsMessageCodec extends MessageToMessageCodec[WebSocketFrame, WsFrame] {

  override def encode(ctx: ChannelHandlerContext, msg: WsFrame, out: java.util.List[AnyRef]): Unit = msg match {
    case CloseWsFrame(status, reason) => out add new CloseWebSocketFrame(status, reason)
    case PingWsFrame(bytes) => out add new PingWebSocketFrame(bytes)
    case PongWsFrame(bytes) => out add new PongWebSocketFrame(bytes)
    case TextWsFrame(text) => out add new TextWebSocketFrame(text)
    case BinaryWsFrame(bytes) => out add new BinaryWebSocketFrame(bytes)
  }

  override def decode(ctx: ChannelHandlerContext, msg: WebSocketFrame, out: java.util.List[AnyRef]): Unit = msg match {
    case frame: CloseWebSocketFrame => out add CloseWsFrame(frame.statusCode, frame.reasonText)
    case frame: PingWebSocketFrame => out add PingWsFrame(frame.content)
    case frame: PongWebSocketFrame => out add PongWsFrame(frame.content)
    case frame: TextWebSocketFrame => out add TextWsFrame(frame.text)
    case frame: BinaryWebSocketFrame => out add BinaryWsFrame(frame.content)
    case _ => ()
  }
}
