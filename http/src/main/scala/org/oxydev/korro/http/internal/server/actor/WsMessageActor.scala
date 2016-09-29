/*
 * Copyright 2016 Vladimir Konstantinov, Yuriy Gintsyak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.oxydev.korro.http.internal.server.actor

import org.oxydev.korro.http.api.ws.{SetTarget, WsConnection, WsFrame}

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

    case SetTarget(ref) =>
      setTargetTimeout.cancel()
      context watch ref
      unstashAll()
      context become {
        case WsMessageActor.Inbound(msg) => ref ! msg
        case msg: WsFrame => channel.writeAndFlush(msg)
        case Terminated(`ref`) => disconnect()
      }

    case ReceiveTimeout =>
      log.warning("SetTarget command was not received in 5 seconds. Closing connection...", route)
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

  case class Inbound(msg: WsFrame)
  case object Disconnect
}
