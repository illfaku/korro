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
package org.oxydev.korro.http.internal.server.actor

import org.oxydev.korro.http.api.ws.{SetTarget, WsMessage}

import akka.actor._
import io.netty.channel.Channel

import scala.concurrent.duration._

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object WsMessageSender {

  def create(channel: Channel)(implicit factory: ActorRefFactory): ActorRef = factory.actorOf(props(channel))

  def props(channel: Channel): Props = Props(new WsMessageSender(channel))

  case class Inbound[T <: WsMessage](msg: T)
  case object Disconnect
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class WsMessageSender(channel: Channel) extends Actor with Stash with ActorLogging {

  import context.dispatcher

  val setTargetTimeout = context.system.scheduler.scheduleOnce(5 seconds, self, ReceiveTimeout)

  override def receive = {

    case ReceiveTimeout =>
      log.error("Command SetTarget was not received in 5 seconds. Closing connection...")
      disconnect()

    case WsMessageSender.Inbound(_) => stash()

    case SetTarget(ref) =>
      setTargetTimeout.cancel()
      context watch ref
      unstashAll()
      context become {
        case WsMessageSender.Inbound(msg) => ref ! msg
        case msg: WsMessage => channel.writeAndFlush(msg)
        case Terminated(`ref`) => disconnect()
      }
  }

  private def disconnect(): Unit = channel.pipeline.fireUserEventTriggered(WsMessageSender.Disconnect)

  override def postStop(): Unit = {
    setTargetTimeout.cancel()
    super.postStop()
  }
}
