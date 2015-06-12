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
package io.cafebabe.http.impl

import akka.actor.{ActorRef, PoisonPill}
import io.cafebabe.http.api.{BinaryWsMessage, DisconnectWsMessage, TextWsMessage}
import io.cafebabe.http.impl.util.ByteBufUtils._
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.websocketx._
import org.slf4j.LoggerFactory

/**
 * @author Vladimir Konstantinov
 * @version 1.0 (6/12/2015)
 */
class WsChannelHandler(receiver: ActorRef, sender: ActorRef) extends SimpleChannelInboundHandler[WebSocketFrame] {

  private val log = LoggerFactory.getLogger(getClass)

  override def channelRead0(ctx: ChannelHandlerContext, msg: WebSocketFrame): Unit = msg match {
    case frame: CloseWebSocketFrame =>
      ctx.channel.writeAndFlush(frame.retain()).addListener(ChannelFutureListener.CLOSE)
      receiver.tell(DisconnectWsMessage, sender)
      sender ! PoisonPill
    case frame: PingWebSocketFrame =>
      ctx.channel.writeAndFlush(new PongWebSocketFrame(frame.content.retain()))
    case frame: BinaryWebSocketFrame =>
      receiver.tell(new BinaryWsMessage(toBytes(frame.content)), sender)
    case frame: TextWebSocketFrame =>
      receiver.tell(new TextWsMessage(frame.text), sender)
    case frame => log.warn("Some unpredicted frame was received: {}.", frame)
  }

  override def channelInactive(ctx: ChannelHandlerContext): Unit = {
    receiver.tell(DisconnectWsMessage, sender)
    sender ! PoisonPill
    super.channelInactive(ctx)
  }
}
