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
package org.oxydev.korro.http.internal.server.handler

import org.oxydev.korro.http.api.ws.{WsConnection, WsMessage}
import org.oxydev.korro.http.internal.server.actor.{HttpServerActor, WsMessageActor}
import org.oxydev.korro.util.log.Logging

import akka.actor.{ActorRef, PoisonPill}
import akka.pattern.ask
import akka.util.Timeout
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext
import scala.concurrent.JavaConversions._
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class WsChannelHandler(parent: ActorRef, connection: WsConnection, route: String)
  extends SimpleChannelInboundHandler[WsMessage] with Logging {

  private var sender: Option[ActorRef] = None

  private val stash = ListBuffer.empty[WsMessage]

  override def handlerAdded(ctx: ChannelHandlerContext): Unit = {
    implicit val ec: ExecutionContext = ctx.channel.eventLoop
    implicit val timeout = Timeout(5 seconds)
    val props = WsMessageActor.props(ctx.channel, route, connection)
    (parent ? HttpServerActor.CreateChild(props, returnRef = true)).mapTo[ActorRef] onComplete {
      case Success(ref) if ctx.channel.isActive =>
        stash foreach (ref ! WsMessageActor.Inbound(_))
        stash.clear()
        sender = Some(ref)
      case Success(ref) => ref ! PoisonPill
      case Failure(cause) =>
        log.error(cause, "Failed to instantiate WsMessageActor.")
        ctx.close()
    }
  }

  override def channelRead0(ctx: ChannelHandlerContext, msg: WsMessage): Unit = sender match {
    case Some(ref) => ref ! WsMessageActor.Inbound(msg)
    case None => stash += msg
  }

  override def userEventTriggered(ctx: ChannelHandlerContext, evt: Any): Unit = evt match {
    case WsMessageActor.Disconnect => ctx.close()
    case _ => ctx.fireUserEventTriggered(evt)
  }

  override def channelInactive(ctx: ChannelHandlerContext): Unit = {
    sender foreach (_ ! PoisonPill)
    ctx.fireChannelInactive()
  }
}
