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

import org.oxydev.korro.http.api.ws.{SetTarget, WsConnection, WsMessage}

import akka.actor._
import io.netty.channel.Channel

import scala.concurrent.duration._

class WsMessageActor(channel: Channel, route: String, init: WsConnection) extends Actor with Stash with ActorLogging {

  import context.dispatcher

  val setTargetTimeout = context.system.scheduler.scheduleOnce(5 seconds, self, ReceiveTimeout)

  override def preStart(): Unit = {
    context.actorSelection(route) ! init
    super.preStart()
  }

  override def receive = {

    case _: WsMessageActor.Inbound => stash()

    case SetTarget(Some(ref)) =>
      setTargetTimeout.cancel()
      context watch ref
      unstashAll()
      context become {
        case WsMessageActor.Inbound(msg) => ref ! msg
        case msg: WsMessage => channel.writeAndFlush(msg)
        case Terminated(`ref`) => disconnect()
      }

    case SetTarget(None) =>
      setTargetTimeout.cancel()
      log.error("No actor for {}. Closing connection...", init)
      disconnect()

    case ReceiveTimeout =>
      log.error("SetTarget command was not received in 5 seconds. Closing connection...", route)
      disconnect()
  }

  private def disconnect(): Unit = channel.pipeline.fireUserEventTriggered(WsMessageActor.Disconnect)

  override def postStop(): Unit = {
    setTargetTimeout.cancel()
    super.postStop()
  }
}

object WsMessageActor {

  def props(channel: Channel, route: String, init: WsConnection): Props = Props(new WsMessageActor(channel, route, init))

  case class Inbound(msg: WsMessage)
  case object Disconnect
}
