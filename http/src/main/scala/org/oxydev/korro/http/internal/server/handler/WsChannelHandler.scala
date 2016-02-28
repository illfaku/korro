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

import org.oxydev.korro.http.api.ws.{Connected, WsMessage}
import org.oxydev.korro.http.internal.server.actor.WsMessageSender
import org.oxydev.korro.util.log.Logging

import akka.actor.{ActorContext, ActorRef, PoisonPill}
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class WsChannelHandler(host: String, route: String)(implicit context: ActorContext)
  extends SimpleChannelInboundHandler[WsMessage] with Logging {

  private var sender: ActorRef = null

  override def handlerAdded(ctx: ChannelHandlerContext): Unit = {
    sender = WsMessageSender.create(ctx.channel)
    context.actorSelection(route).tell(Connected(host), sender)
  }

  override def channelRead0(ctx: ChannelHandlerContext, msg: WsMessage): Unit = sender ! WsMessageSender.Inbound(msg)

  override def userEventTriggered(ctx: ChannelHandlerContext, evt: Any): Unit = evt match {
    case WsMessageSender.Disconnect => ctx.close()
    case _ => ctx.fireUserEventTriggered(evt)
  }

  override def channelInactive(ctx: ChannelHandlerContext): Unit = {
    sender ! PoisonPill
    ctx.fireChannelInactive()
  }
}
