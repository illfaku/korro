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

import org.oxydev.korro.http.api.HttpResponse.Status.{RequestTimeout, ServerError}
import org.oxydev.korro.http.api.{HttpRequest, HttpResponse}
import org.oxydev.korro.http.internal.common.ChannelFutureExt
import org.oxydev.korro.http.internal.server.config.HttpConfig

import akka.actor._
import io.netty.channel.Channel

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class HttpMessageActor(channel: Channel, config: HttpConfig, route: String, req: HttpRequest)
  extends Actor with ActorLogging {

  override def preStart(): Unit = {
    context.actorSelection(route) ! req
    context.setReceiveTimeout(config.requestTimeout)
    super.preStart()
  }

  override def receive = {
    case res: HttpResponse => send(res)
    case Status.Success(res: HttpResponse) => send(res)
    case Status.Failure(cause) =>
      log.error(cause, "Received failure instead of HttpResponse for {}.", req)
      send(ServerError())
    case ReceiveTimeout =>
      log.error("HttpResponse for {} was not received in time.", req)
      send(RequestTimeout())
  }

  private def send(res: HttpResponse): Unit = {
    channel.writeAndFlush(res).closeChannel()
    context.stop(self)
  }
}

object HttpMessageActor {
  def props(channel: Channel, config: HttpConfig, route: String, req: HttpRequest): Props = {
    Props(new HttpMessageActor(channel, config, route, req))
  }
}
