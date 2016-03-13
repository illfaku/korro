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
