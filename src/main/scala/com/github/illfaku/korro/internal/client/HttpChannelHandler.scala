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

import com.github.illfaku.korro.dto.{HttpRequest, HttpResponse}
import com.github.illfaku.korro.internal.common.HttpInstructions

import akka.actor.{ActorRef, Status}
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}

import java.util.concurrent.{ScheduledFuture, TimeoutException}

import scala.util.control.NoStackTrace

private[client] class HttpChannelHandler(req: HttpRequest, originator: ActorRef, instructions: HttpInstructions)
  extends SimpleChannelInboundHandler[HttpResponse] {

  private var timeoutTask: ScheduledFuture[_] = _

  override def handlerAdded(ctx: ChannelHandlerContext) = {
    timeoutTask = ctx.channel.eventLoop.schedule(
      new Runnable { override def run() = ctx.channel.pipeline.fireUserEventTriggered(TimedOut) },
      instructions.requestTimeout.length,
      instructions.requestTimeout.unit
    )
  }

  override def channelActive(ctx: ChannelHandlerContext): Unit = {
    ctx.writeAndFlush(req, ctx.voidPromise)
    super.channelActive(ctx)
  }

  override def channelRead0(ctx: ChannelHandlerContext, msg: HttpResponse): Unit = complete(ctx, msg)

  override def userEventTriggered(ctx: ChannelHandlerContext, evt: Any): Unit = evt match {
    case TimedOut => complete(ctx, Status.Failure(new TimeoutException("Request has timed out.") with NoStackTrace))
    case _ => super.userEventTriggered(ctx, evt)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    complete(ctx, Status.Failure(cause))
  }

  override def channelInactive(ctx: ChannelHandlerContext): Unit = {
    complete(ctx, Status.Failure(new IllegalStateException("Channel was closed before response.") with NoStackTrace))
    super.channelInactive(ctx)
  }

  private def complete(ctx: ChannelHandlerContext, result: Any): Unit = {
    if (timeoutTask != null) timeoutTask.cancel(false)
    originator ! result
    ctx.close()
  }
}
