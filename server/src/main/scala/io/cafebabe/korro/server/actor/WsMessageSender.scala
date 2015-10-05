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
package io.cafebabe.korro.server.actor

import io.cafebabe.korro.api.ws.{PingWsMessage, BinaryWsMessage, TextWsMessage}
import io.cafebabe.korro.server.util.ByteBufUtils.toByteBuf

import akka.actor.{ActorRef, ActorRefFactory, Actor, Props}
import io.netty.channel.{ChannelHandlerContext, Channel, ChannelFuture, ChannelFutureListener}
import io.netty.handler.codec.http.websocketx._

import java.util.concurrent.atomic.AtomicLong

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object WsMessageSender {

  private val counter = new AtomicLong

  def create(ctx: ChannelHandlerContext)(implicit factory: ActorRefFactory): ActorRef = {
    factory.actorOf(Props(new WsMessageSender(ctx)), "ws-sender-" + counter.incrementAndGet())
  }
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class WsMessageSender(ctx: ChannelHandlerContext) extends Actor {

  override def receive = {
    case PingWsMessage => send(new PingWebSocketFrame())
    case TextWsMessage(text) => send(new TextWebSocketFrame(text))
    case BinaryWsMessage(bytes) => send(new BinaryWebSocketFrame(toByteBuf(bytes)))
  }

  override def postStop(): Unit = send(new CloseWebSocketFrame(1001, null)).addListener(ChannelFutureListener.CLOSE)

  private def send(frame: WebSocketFrame): ChannelFuture = ctx.writeAndFlush(frame)
}
