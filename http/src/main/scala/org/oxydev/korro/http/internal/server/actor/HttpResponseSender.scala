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
package org.oxydev.korro.http.internal.server.actor

import org.oxydev.korro.http.api.HttpResponse
import org.oxydev.korro.http.api.HttpResponse.Status.RequestTimeout
import org.oxydev.korro.http.internal.common.ChannelFutureExt

import akka.actor._
import io.netty.channel.ChannelHandlerContext

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
    ctx.writeAndFlush(res).closeChannel()
    context.stop(self)
  }
}
