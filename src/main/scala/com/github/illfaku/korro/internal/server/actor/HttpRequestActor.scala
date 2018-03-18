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
package com.github.illfaku.korro.internal.server.actor

import org.oxydev.korro.api.HttpResponse.Status.{RequestTimeout, ServerError}
import org.oxydev.korro.internal.common.ChannelFutureExt
import com.github.illfaku.korro.internal.server.route.MergedRouteInstructions
import akka.actor.{Actor, ActorLogging, Props, ReceiveTimeout, Status}
import com.github.illfaku.korro.dto.HttpResponse
import io.netty.channel.Channel

class HttpRequestActor(channel: Channel, instructions: MergedRouteInstructions, info: String)
  extends Actor with ActorLogging {

  context.setReceiveTimeout(instructions.requestTimeout)

  override def receive = {
    case res: HttpResponse => send(res)
    case Status.Success(res: HttpResponse) => send(res)
    case Status.Failure(cause) =>
      log.error(cause, "[{}] Received failure instead of HTTP response.", info)
      send(ServerError())
    case ReceiveTimeout =>
      log.error("[{}] HTTP response was not received in time.", info)
      send(RequestTimeout())
  }

  private def send(res: HttpResponse): Unit = {
    channel.writeAndFlush(res).closeChannel()
    context stop self
  }
}

object HttpRequestActor {
  def props(channel: Channel, instructions: MergedRouteInstructions, info: String): Props = {
    Props(new HttpRequestActor(channel, instructions, info))
  }
}

