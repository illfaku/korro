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

import io.cafebabe.korro.api.ws._
import io.cafebabe.korro.server.actor.WsMessageSender
import io.cafebabe.korro.server.actor.WsMessageSender.Inbound
import io.cafebabe.korro.util.log.Logging

import akka.actor.{ActorContext, ActorRef}
import io.netty.channel._

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class WsChannelHandler(host: String, route: String)(implicit context: ActorContext)
  extends SimpleChannelInboundHandler[WsMessage] with Logging {

  private var sender: ActorRef = null

  override def handlerAdded(ctx: ChannelHandlerContext): Unit = {
    sender = WsMessageSender.create(ctx)
    context.actorSelection(route) ! ConnectWsMessage(host)
  }

  override def channelRead0(ctx: ChannelHandlerContext, msg: WsMessage): Unit = sender ! Inbound(msg)

  override def channelInactive(ctx: ChannelHandlerContext): Unit = sender ! Inbound(DisconnectWsMessage)
}
