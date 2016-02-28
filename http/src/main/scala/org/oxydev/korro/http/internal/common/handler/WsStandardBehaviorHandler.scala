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
