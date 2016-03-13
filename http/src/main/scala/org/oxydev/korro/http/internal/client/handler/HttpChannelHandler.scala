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
package org.oxydev.korro.http.internal.client.handler

import org.oxydev.korro.http.api.{HttpRequest, HttpResponse}
import org.oxydev.korro.http.internal.common.ChannelFutureExt

import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}

import scala.concurrent.Promise
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success, Try}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class HttpChannelHandler(req: HttpRequest, promise: Promise[HttpResponse], timeout: FiniteDuration)
  extends SimpleChannelInboundHandler[HttpResponse] {

  private case object TimedOut

  override def channelActive(ctx: ChannelHandlerContext): Unit = {

    ctx.channel.eventLoop.schedule(new Runnable {
      override def run(): Unit = ctx.channel.pipeline.fireUserEventTriggered(TimedOut)
    }, timeout.length, timeout.unit)

    ctx.writeAndFlush(req) foreach { f => if (!f.isSuccess) complete(ctx, Failure(f.cause)) }

    super.channelActive(ctx)
  }

  override def channelRead0(ctx: ChannelHandlerContext, msg: HttpResponse): Unit = complete(ctx, Success(msg))

  override def userEventTriggered(ctx: ChannelHandlerContext, evt: Any): Unit = evt match {
    case TimedOut => complete(ctx, Failure(new IllegalStateException("Request has timed out.")))
    case _ => super.userEventTriggered(ctx, evt)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = complete(ctx, Failure(cause))

  override def channelInactive(ctx: ChannelHandlerContext): Unit = {
    complete(ctx, Failure(new IllegalStateException("Channel was closed before response.")))
    super.channelInactive(ctx)
  }

  private def complete(ctx: ChannelHandlerContext, status: Try[HttpResponse]): Unit = {
    promise tryComplete status
    ctx.close()
  }
}
