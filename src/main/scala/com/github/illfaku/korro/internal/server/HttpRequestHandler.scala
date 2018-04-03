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

import com.github.illfaku.korro.dto.{HttpRequest, HttpResponse}
import com.github.illfaku.korro.internal.common.{ChannelFutureExt, TimedOut}
import com.github.illfaku.korro.internal.server.Routing.DstRoute
import com.github.illfaku.korro.util.logging.Logging

import akka.actor.{ActorRef, PoisonPill}
import io.netty.channel.{ChannelDuplexHandler, ChannelHandlerContext, ChannelPromise}

import java.util.concurrent.ScheduledFuture

private[server] class HttpRequestHandler(route: DstRoute)(implicit outActor: ActorRef)
  extends ChannelDuplexHandler with Logging {

  private var timeoutTask: ScheduledFuture[_] = _

  private var responseSent = false

  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = msg match {
    case req: HttpRequest =>
      route ! req
      timeoutTask = ctx.channel.eventLoop.schedule(
        new Runnable { override def run() = ctx.channel.pipeline.fireUserEventTriggered(TimedOut) },
        route.instructions.requestTimeout.length,
        route.instructions.requestTimeout.unit
      )
    case _ => super.channelRead(ctx, msg)
  }

  override def write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) = if (!responseSent) {
    if (timeoutTask != null) timeoutTask.cancel(false)
    val newPromise = promise.unvoid
    newPromise foreach { f =>
      outActor ! PoisonPill
      f.channel.close()
    }
    ctx.write(msg, newPromise)
    responseSent = true
  }

  override def userEventTriggered(ctx: ChannelHandlerContext, evt: Any) = evt match {
    case TimedOut => write(ctx, HttpResponse.Status.RequestTimeout(), ctx.newPromise)
    case _ => super.userEventTriggered(ctx, evt)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) = {
    log.error(cause, "Korro | Server failure.")
    write(ctx, HttpResponse.Status.ServerError(), ctx.newPromise)
  }

  private def close(ctx: ChannelHandlerContext): Unit = {
    outActor ! PoisonPill
    ctx.close()
  }
}
