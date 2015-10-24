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

import io.cafebabe.korro.api.http.HttpRequest
import io.cafebabe.korro.netty.ChannelFutureExt
import io.cafebabe.korro.netty.convert.HttpRequestConverter.toNetty
import io.cafebabe.korro.netty.convert.HttpResponseConverter.fromNetty

import akka.actor.{ActorRef, Status}
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.{FullHttpResponse, HttpHeaders}

import java.net.URL

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class HttpChannelHandler(url: URL, req: HttpRequest, sender: ActorRef)
  extends SimpleChannelInboundHandler[FullHttpResponse] {

  override def channelActive(ctx: ChannelHandlerContext): Unit = {
    val request = toNetty(req)
    request.headers().add(HttpHeaders.Names.HOST, url.getHost)

    ctx.writeAndFlush(request) foreach { future =>
      if (!future.isSuccess) {
        sender ! Status.Failure(future.cause)
        future.channel.close()
      }
    }
  }

  override def channelRead0(ctx: ChannelHandlerContext, msg: FullHttpResponse): Unit = {
    fromNetty(msg) match {
      case Right(res) => sender ! Status.Success(res)
      case Left(fail) => sender ! Status.Failure(new Exception(s"Conversion failure. $fail"))
    }
    ctx.close()
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    sender ! Status.Failure(cause)
    ctx.close()
  }
}
