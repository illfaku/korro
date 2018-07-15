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
package io.cafebabe.korro.server.actor

import io.cafebabe.korro.api.http.HttpResponse
import io.cafebabe.korro.api.http.HttpStatus._

import akka.actor._
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext}

import scala.concurrent.duration.FiniteDuration

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object HttpResponseSender {
  def create(ctx: ChannelHandlerContext, timeout: FiniteDuration)(implicit factory: ActorRefFactory): ActorRef = {
    factory.actorOf(Props(new HttpResponseSender(ctx, timeout)))
  }
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class HttpResponseSender(ctx: ChannelHandlerContext, timeout: FiniteDuration) extends Actor {

  context.setReceiveTimeout(timeout)

  override def receive = {
    case res: HttpResponse => send(res)
    case ReceiveTimeout => send(RequestTimeout())
  }

  private def send(res: HttpResponse): Unit = {
    ctx.writeAndFlush(res).addListener(ChannelFutureListener.CLOSE)
    context.stop(self)
  }
}
