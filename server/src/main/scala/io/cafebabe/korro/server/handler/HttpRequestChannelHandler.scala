/*
 * Copyright (C) 2015  Vladimir Konstantinov, Yuriy Gintsyak
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
package io.cafebabe.korro.server.handler

import io.cafebabe.korro.api.http.HttpRequest
import io.cafebabe.korro.api.route.HttpRoute
import io.cafebabe.korro.server.actor.HttpResponseSender
import io.cafebabe.korro.server.config.HttpConfig

import akka.actor.ActorContext
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class HttpRequestChannelHandler(config: HttpConfig, route: HttpRoute)(implicit context: ActorContext)
  extends SimpleChannelInboundHandler[HttpRequest] {

  override def channelRead0(ctx: ChannelHandlerContext, msg: HttpRequest): Unit = {
    implicit val sender = HttpResponseSender.create(ctx, config.requestTimeout)
    context.actorSelection(route.actor) ! msg
  }
}
