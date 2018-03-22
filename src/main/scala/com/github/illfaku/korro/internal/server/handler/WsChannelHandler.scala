/*
 * Copyright 2018 Vladimir Konstantinov
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
package com.github.illfaku.korro.internal.server.handler

import org.oxydev.korro.api.ws.WsFrame
import com.github.illfaku.korro.internal.server.actor.{ServerActor, WsMessageActor}
import com.github.illfaku.korro.util.logging.Logging
import akka.actor.{ActorRef, PoisonPill}
import akka.pattern.ask
import akka.util.Timeout
import com.github.illfaku.korro.dto.ws.{WsHandshakeRequest, WsFrame}
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext
import scala.concurrent.JavaConversions._
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class WsChannelHandler(parent: ActorRef, connection: WsConnection, route: String)
  extends SimpleChannelInboundHandler[WsFrame] with Logging {

  private var sender: Option[ActorRef] = None

  private val stash = ListBuffer.empty[WsFrame]

  override def handlerAdded(ctx: ChannelHandlerContext): Unit = {
    implicit val ec: ExecutionContext = ctx.channel.eventLoop
    implicit val timeout = Timeout(5 seconds)
    val props = WsMessageActor.props(ctx.channel, route, connection)
//    (parent ? HttpServerActor.CreateChild(props, returnRef = true)).mapTo[ActorRef] onComplete {
//      case Success(ref) if ctx.channel.isActive =>
//        stash foreach (ref ! WsMessageActor.Inbound(_))
//        stash.clear()
//        sender = Some(ref)
//      case Success(ref) => ref ! PoisonPill
//      case Failure(cause) =>
//        log.error(cause, "Failed to instantiate WsMessageActor.")
//        ctx.close()
//    }
  }

  override def channelRead0(ctx: ChannelHandlerContext, msg: WsFrame): Unit = sender match {
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
