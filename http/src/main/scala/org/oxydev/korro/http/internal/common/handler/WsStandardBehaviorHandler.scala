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

import org.oxydev.korro.http.internal.common.ChannelFutureExt

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelDuplexHandler, ChannelHandlerContext, ChannelPromise}
import io.netty.handler.codec.http.websocketx.{CloseWebSocketFrame, PingWebSocketFrame, PongWebSocketFrame}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
@Sharable
object WsStandardBehaviorHandler extends ChannelDuplexHandler {

  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = msg match {
    case frame: PingWebSocketFrame => ctx.writeAndFlush(new PongWebSocketFrame(frame.content))
    case frame: CloseWebSocketFrame => ctx.writeAndFlush(frame).foreach(_ => ctx.close())
    case _ => ctx.fireChannelRead(msg)
  }

  override def close(ctx: ChannelHandlerContext, future: ChannelPromise): Unit = {
    ctx.writeAndFlush(new CloseWebSocketFrame(1001, "Shutdown")).foreach(_ => ctx.close(future))
  }
}
