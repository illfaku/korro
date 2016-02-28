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
package org.oxydev.korro.http.server.actor

import org.oxydev.korro.http.api.ws._
import org.oxydev.korro.http.internal.ChannelFutureExt

import akka.actor._
import io.netty.channel.{ChannelFuture, ChannelHandlerContext}

import scala.concurrent.duration._

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
class WsMessageSender(ctx: ChannelHandlerContext) extends Actor with Stash with ActorLogging {

  import context.dispatcher

  var setRecipientTimeout: Cancellable = null

  override def preStart(): Unit = {
    setRecipientTimeout = context.system.scheduler.scheduleOnce(5 seconds, self, ReceiveTimeout)
    super.preStart()
  }

  import WsMessageSender.Inbound

  override def receive = {

    case ReceiveTimeout =>
      log.error("Command SetRecipient was not received in 5 seconds. Closing connection...")
      context.stop(self)

    case Inbound(_) => stash()

    case SetTarget(ref) =>
      setRecipientTimeout.cancel()
      unstashAll()
      context become {
        case Inbound(DisconnectWsMessage) => self ! PoisonPill
        case DisconnectWsMessage => self ! PoisonPill
        case Inbound(msg) => ref ! msg
        case msg: WsMessage => send(msg)
      }
  }

  override def postStop(): Unit = {
    setRecipientTimeout.cancel()
    send(DisconnectWsMessage).closeChannel()
    super.postStop()
  }

  private def send(msg: Any): ChannelFuture = ctx.writeAndFlush(msg)
}
