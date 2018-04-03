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
package com.github.illfaku.korro.internal.client

import com.github.illfaku.korro.dto.ws.{CloseWsFrame, WsFrame, WsHandshakeResponse}
import com.github.illfaku.korro.internal.common.ChannelFutureExt
import com.github.illfaku.korro.internal.common.HttpActorFactory.NewHttpActor

import akka.actor.{ActorRef, PoisonPill, Status}
import akka.pattern.ask
import akka.util.Timeout
import io.netty.channel.{ChannelDuplexHandler, ChannelHandlerContext, ChannelPromise}
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler.ClientHandshakeStateEvent

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.control.NoStackTrace

private[client] class WsChannelHandler(parent: ActorRef, inActor: ActorRef) extends ChannelDuplexHandler {

  private implicit val timeout = Timeout(5 seconds)

  private var outActor: ActorRef = _

  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = {
    msg match {
      case f: CloseWsFrame =>
        inActor ! f
        close(ctx)
      case f: WsFrame => inActor ! f
      case _ => super.channelRead(ctx, msg)
    }
  }

  override def write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) = {
    if (msg.isInstanceOf[CloseWsFrame]) {
      val newPromise = promise.unvoid
      newPromise.foreach(_ => close(ctx))
      super.write(ctx, msg, newPromise)
    } else {
      super.write(ctx, msg, promise)
    }
  }

  override def userEventTriggered(ctx: ChannelHandlerContext, evt: Any): Unit = evt match {
    case ClientHandshakeStateEvent.HANDSHAKE_COMPLETE =>
      outActor = Await.result((parent ? NewHttpActor(ctx.channel)).mapTo[ActorRef], Duration.Inf)
      inActor ! WsHandshakeResponse(outActor)
    case _ => super.userEventTriggered(ctx, evt)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    inActor ! Status.Failure(cause)
    close(ctx)
  }

  override def channelInactive(ctx: ChannelHandlerContext): Unit = {
    inActor ! Status.Failure(new IllegalStateException("Channel was closed unexpectedly.") with NoStackTrace)
    close(ctx)
    super.channelInactive(ctx)
  }

  private def close(ctx: ChannelHandlerContext): Unit = {
    if (outActor != null) outActor ! PoisonPill
    ctx.close()
  }
}
