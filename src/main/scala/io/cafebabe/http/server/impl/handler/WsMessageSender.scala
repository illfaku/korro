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
package io.cafebabe.http.server.impl.handler

import io.cafebabe.http.server.api.{BinaryWsMessage, TextWsMessage}
import io.cafebabe.http.server.impl.util.ByteBufUtils.toByteBuf

import akka.actor.{Actor, Props}
import io.netty.channel.{Channel, ChannelFuture, ChannelFutureListener}
import io.netty.handler.codec.http.websocketx._

import java.util.concurrent.atomic.AtomicLong

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 * @version 1.0 (6/12/2015)
 */
object WsMessageSender {
  private val counter = new AtomicLong
  def name = "ws-" + counter.incrementAndGet()
  def props(channel: Channel) = Props(new WsMessageSender(channel))
}

class WsMessageSender(channel: Channel) extends Actor {
  override def receive = {
    case TextWsMessage(text) => send(new TextWebSocketFrame(text))
    case BinaryWsMessage(bytes) => send(new BinaryWebSocketFrame(toByteBuf(bytes)))
  }

  override def postStop(): Unit = send(new CloseWebSocketFrame(1001, null)).addListener(ChannelFutureListener.CLOSE)

  private def send(frame: WebSocketFrame): ChannelFuture = channel.writeAndFlush(frame)
}
