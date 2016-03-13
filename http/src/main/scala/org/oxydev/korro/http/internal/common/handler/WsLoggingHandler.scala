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
