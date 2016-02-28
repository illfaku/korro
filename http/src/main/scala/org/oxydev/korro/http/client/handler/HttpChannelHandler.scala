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
package org.oxydev.korro.http.client.handler

import org.oxydev.korro.http.api.{HttpRequest, HttpResponse}
import org.oxydev.korro.http.internal.ChannelFutureExt

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
