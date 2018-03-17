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
package com.github.illfaku.korro.internal.server.handler

import com.github.illfaku.korro.util.log.Logging

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerAdapter, ChannelHandlerContext}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
@Sharable
class LastChannelHandler extends ChannelHandlerAdapter with Logging {

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    log.error(cause, "Uncaught error at the end of pipeline. Closing channel...")
    ctx.close()
  }
}
