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

import io.cafebabe.korro.api.ws._

import akka.actor._
import io.netty.channel.{ChannelFuture, ChannelHandlerContext}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object WsMessageSender {

  def create(ctx: ChannelHandlerContext)(implicit factory: ActorRefFactory): ActorRef = {
    factory.actorOf(Props(new WsMessageSender(ctx)))
  }

  case class Inbound[T <: WsMessage](msg: T)
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class WsMessageSender(ctx: ChannelHandlerContext) extends Actor with Stash {

  import WsMessageSender.Inbound

  override def receive = {
    case msg: WsMessage => send(msg)
    case _: Inbound[_] => stash()
    case SetRecipient(ref) =>
      unstashAll()
      context become {
        case msg: WsMessage => send(msg)
        case Inbound(msg) => ref ! msg
      }
  }

  override def postStop(): Unit = ctx.close()

  private def send(msg: WsMessage): ChannelFuture = ctx.writeAndFlush(msg)
}
