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

import io.cafebabe.korro.api.http.route.HttpRoute
import io.cafebabe.korro.server.actor.HttpResponseSender
import io.cafebabe.korro.server.convert.HttpRequestConverter

import akka.actor.ActorContext
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter}
import io.netty.handler.codec.http.FullHttpRequest

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class HttpRequestChannelHandler(implicit context: ActorContext) extends ChannelInboundHandlerAdapter {

  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = msg match {
    case RoutedHttpRequest(req, route) =>
      val sender = HttpResponseSender.create(ctx, route.requestTimeout)
      context.actorSelection(route.actor).tell(HttpRequestConverter.fromNetty(req, route.path), sender)
      req.release()
    case _ => ctx.fireChannelRead(msg)
  }
}

case class RoutedHttpRequest(req: FullHttpRequest, route: HttpRoute)
