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

import org.oxydev.korro.util.log.Logger

import io.netty.channel.{ChannelPromise, ChannelHandlerContext, ChannelDuplexHandler}
import io.netty.handler.codec.http.websocketx._

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class WsLoggingHandler(name: String) extends ChannelDuplexHandler {

  private val logger = Logger(name)

  private var id: String = null

  private def enabled: Boolean = logger.isTraceEnabled

  private def log(message: String): Unit = {
    logger.trace("[{}] {}", id, message)
  }

  override def handlerAdded(ctx: ChannelHandlerContext): Unit = {
    id = f"0x${ctx.channel.hashCode}%08x"
    if (enabled) log("CONNECTED")
    super.handlerAdded(ctx)
  }

  override def channelInactive(ctx: ChannelHandlerContext): Unit = {
    if (enabled) log("DISCONNECTED")
    super.channelInactive(ctx)
  }

  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = {
    if (enabled) text(msg) foreach { t => log(">>> " + t) }
    super.channelRead(ctx, msg)
  }

  override def write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise): Unit = {
    if (enabled) text(msg) foreach { t => log("<<< " + t) }
    super.write(ctx, msg, promise)
  }

  private def text(msg: Any): Option[String] = msg match {
    case m: TextWebSocketFrame => Some(m.text)
    case m: BinaryWebSocketFrame => Some(s"BINARY(${m.content.readableBytes}B)")
    case m: CloseWebSocketFrame => Some(s"CLOSE(${m.statusCode}, ${m.reasonText})")
    case m: PingWebSocketFrame => Some(s"PING(${m.content.readableBytes}B)")
    case m: PongWebSocketFrame => Some(s"PONG(${m.content.readableBytes}B)")
    case _ => None
  }
}
