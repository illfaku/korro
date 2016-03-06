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

import org.oxydev.korro.http.api.ws.{Connected, WsMessage}

import akka.actor._
import io.netty.channel.Channel

import scala.concurrent.duration._

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class WsMessageActor(channel: Channel, route: String, init: Connected) extends Actor with Stash with ActorLogging {

  import context.dispatcher

  val identifyTimeout = context.system.scheduler.scheduleOnce(5 seconds, self, ReceiveTimeout)

  override def preStart(): Unit = {
    context.actorSelection(route) ! Identify('route)
    super.preStart()
  }

  override def receive = {

    case _: WsMessageActor.Inbound => stash()

    case ActorIdentity('route, Some(ref)) =>
      identifyTimeout.cancel()
      context watch ref
      ref ! init
      unstashAll()
      context become {
        case WsMessageActor.Inbound(msg) => ref ! msg
        case msg: WsMessage => channel.writeAndFlush(msg)
        case Terminated(`ref`) => disconnect()
      }

    case ActorIdentity('route, None) =>
      identifyTimeout.cancel()
      log.error("Actor {} was not found. Closing connection...", route)
      disconnect()

    case ReceiveTimeout =>
      log.error("Actor {} has not responded in 5 seconds. Closing connection...", route)
      disconnect()
  }

  private def disconnect(): Unit = channel.pipeline.fireUserEventTriggered(WsMessageActor.Disconnect)

  override def postStop(): Unit = {
    identifyTimeout.cancel()
    super.postStop()
  }
}

object WsMessageActor {

  def props(channel: Channel, route: String, init: Connected): Props = Props(new WsMessageActor(channel, route, init))

  case class Inbound(msg: WsMessage)
  case object Disconnect
}