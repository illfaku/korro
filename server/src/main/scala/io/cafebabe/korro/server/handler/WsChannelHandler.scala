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
import io.cafebabe.korro.internal.ByteBufUtils.toBytes
import io.cafebabe.korro.internal.ChannelFutureExt
import io.cafebabe.korro.server.actor.WsMessageSender
import io.cafebabe.korro.util.log.Logging

import akka.actor.{ActorContext, ActorPath, ActorRef, ActorSelection}
import io.netty.channel._
import io.netty.handler.codec.http.websocketx._

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class WsChannelHandler(host: String, actor: String)(implicit context: ActorContext)
  extends SimpleChannelInboundHandler[WebSocketFrame] with Logging {

  private var receiver: ActorSelection = null

  private var sender: ActorRef = null

  override def handlerAdded(ctx: ChannelHandlerContext): Unit = {
    receiver = context.actorSelection(actor)
    sender = WsMessageSender.create(ctx)
    receiver.tell(new ConnectWsMessage(host), sender)
  }

  override def channelRead0(ctx: ChannelHandlerContext, msg: WebSocketFrame): Unit = msg match {
    case frame: CloseWebSocketFrame =>
      ctx.writeAndFlush(frame.retain()) foreach { future =>
        future.channel.close()
        finish()
      }
    case frame: PingWebSocketFrame =>
      ctx.writeAndFlush(new PongWebSocketFrame(frame.content.retain()))
    case frame: PongWebSocketFrame =>
      receiver.tell(PongWsMessage, sender)
    case frame: BinaryWebSocketFrame =>
      receiver.tell(new BinaryWsMessage(frame.content), sender)
    case frame: TextWebSocketFrame =>
      receiver.tell(new TextWsMessage(frame.text), sender)
    case frame =>
      log.debug(s"Unknown frame from host $host: $frame.")
  }

  override def channelInactive(ctx: ChannelHandlerContext): Unit = finish()

  private def finish(): Unit = {
    receiver.tell(DisconnectWsMessage, sender)
    context.stop(sender)
  }
}
