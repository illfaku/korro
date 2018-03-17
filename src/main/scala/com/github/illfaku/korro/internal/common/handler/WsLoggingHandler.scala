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
package com.github.illfaku.korro.internal.common.handler

import com.github.illfaku.korro.util.log.Logger

import io.netty.channel.{ChannelDuplexHandler, ChannelHandlerContext, ChannelPromise}

class WsLoggingHandler(name: String) extends ChannelDuplexHandler {

  private val logger = Logger(name)

  private var id: String = _

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
    if (enabled) log(">>> " + msg)
    super.channelRead(ctx, msg)
  }

  override def write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise): Unit = {
    if (enabled) log("<<< " + msg)
    super.write(ctx, msg, promise)
  }
}
