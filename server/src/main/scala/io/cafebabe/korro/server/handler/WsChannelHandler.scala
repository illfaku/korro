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

import io.cafebabe.korro.api.ws.{BinaryWsMessage, DisconnectWsMessage, TextWsMessage}
import io.cafebabe.korro.server.util.ByteBufUtils.toBytes

import akka.actor.{ActorRef, PoisonPill}
import io.netty.channel._
import io.netty.handler.codec.http.websocketx._
import org.slf4j.LoggerFactory

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class WsChannelHandler(host: String, receiver: ActorRef, sender: ActorRef)
  extends SimpleChannelInboundHandler[WebSocketFrame] {

  private val log = LoggerFactory.getLogger(getClass)

  override def channelRead0(ctx: ChannelHandlerContext, msg: WebSocketFrame): Unit = msg match {
    case frame: CloseWebSocketFrame =>
      log.trace("Close frame from host {}.", host)
      ctx.channel.writeAndFlush(frame.retain()).addListener(new ChannelFutureListener {
        override def operationComplete(future: ChannelFuture): Unit = {
          future.channel.close()
          receiver.tell(DisconnectWsMessage, sender)
          sender ! PoisonPill
        }
      })
    case frame: PingWebSocketFrame =>
      log.trace("Ping frame from host {}.", host)
      ctx.channel.writeAndFlush(new PongWebSocketFrame(frame.content.retain()))
    case frame: PongWebSocketFrame =>
      log.trace("Pong frame from host {}.", host)
    case frame: BinaryWebSocketFrame =>
      receiver.tell(new BinaryWsMessage(toBytes(frame.content)), sender)
    case frame: TextWebSocketFrame =>
      receiver.tell(new TextWsMessage(frame.text), sender)
    case frame => log.debug("Some unpredicted frame was received: {}.", frame)
  }

  override def channelInactive(ctx: ChannelHandlerContext): Unit = {
    receiver.tell(DisconnectWsMessage, sender)
    sender ! PoisonPill
    super.channelInactive(ctx)
  }
}
