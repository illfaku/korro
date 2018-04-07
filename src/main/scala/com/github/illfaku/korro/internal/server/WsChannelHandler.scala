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
package com.github.illfaku.korro.internal.server

import com.github.illfaku.korro.dto.ws.{CloseWsFrame, WsFrame}
import com.github.illfaku.korro.internal.common.ChannelFutureExt

import akka.actor.{ActorRef, PoisonPill, Status}
import io.netty.channel.{ChannelDuplexHandler, ChannelHandlerContext, ChannelPromise}

private[server] class WsChannelHandler(outActor: ActorRef, inActor: ActorRef) extends ChannelDuplexHandler {

  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = {
    msg match {
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

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    inActor ! Status.Failure(cause)
    close(ctx)
  }

  override def channelInactive(ctx: ChannelHandlerContext): Unit = {
    inActor ! CloseWsFrame(1000, "Client has gone.")
    close(ctx)
    super.channelInactive(ctx)
  }

  private def close(ctx: ChannelHandlerContext): Unit = {
    outActor ! PoisonPill
    ctx.close()
  }
}
