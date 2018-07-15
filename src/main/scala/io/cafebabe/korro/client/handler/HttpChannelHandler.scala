/*
 * Copyright (C) 2015  Vladimir Konstantinov, Yuriy Gintsyak
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
package io.cafebabe.korro.client.handler

import io.cafebabe.korro.api.http.{HttpRequest, HttpResponse}
import io.cafebabe.korro.internal.ChannelFutureExt

import akka.actor.{ActorRef, Status}
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.HttpHeaders

import java.net.URL

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class HttpChannelHandler(url: URL, req: HttpRequest, sender: ActorRef)
  extends SimpleChannelInboundHandler[HttpResponse] {

  override def channelActive(ctx: ChannelHandlerContext): Unit = {
    val request = req.copy(headers = req.headers + (HttpHeaders.Names.HOST -> url.getHost))
    ctx.writeAndFlush(request) foreach { future =>
      if (!future.isSuccess) {
        sender ! Status.Failure(future.cause)
        future.channel.close()
      }
    }
  }

  override def channelRead0(ctx: ChannelHandlerContext, msg: HttpResponse): Unit = {
    sender ! Status.Success(msg)
    ctx.close()
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    sender ! Status.Failure(cause)
    ctx.close()
  }
}
