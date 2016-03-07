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
package org.oxydev.korro.http.internal.server.handler

import org.oxydev.korro.http.api.HttpRequest
import org.oxydev.korro.http.internal.server.actor.{HttpMessageActor, HttpServerActor}
import org.oxydev.korro.http.internal.server.config.HttpConfig

import akka.actor.ActorRef
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class HttpRequestHandler(config: HttpConfig, parent: ActorRef, route: String)
  extends SimpleChannelInboundHandler[HttpRequest] {

  override def channelRead0(ctx: ChannelHandlerContext, msg: HttpRequest): Unit = {
    val props = HttpMessageActor.props(ctx.channel, config, route, msg)
    parent ! HttpServerActor.CreateChild(props, returnRef = false)
  }
}
