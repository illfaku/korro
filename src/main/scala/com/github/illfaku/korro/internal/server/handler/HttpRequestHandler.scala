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

import com.github.illfaku.korro.api.HttpRequest
import com.github.illfaku.korro.internal.server.Keys
import com.github.illfaku.korro.internal.server.actor.HttpRequestParentActor
import com.github.illfaku.korro.internal.server.route.RouteInfo
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}

class HttpRequestHandler(route: RouteInfo) extends SimpleChannelInboundHandler[HttpRequest] {

  override def channelRead0(ctx: ChannelHandlerContext, msg: HttpRequest): Unit = {
    val parent = ctx.channel.attr(Keys.reqParent).get
    parent ! HttpRequestParentActor.NewRequest(ctx.channel, route, msg)
  }
}
