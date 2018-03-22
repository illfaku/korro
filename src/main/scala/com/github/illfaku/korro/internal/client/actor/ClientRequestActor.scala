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
package com.github.illfaku.korro.internal.client.actor

import com.github.illfaku.korro.config.ClientConfig
import com.github.illfaku.korro.dto.{HttpRequest, HttpResponse}
import com.github.illfaku.korro.internal.client.handler.HttpChannelInitializer

import akka.actor._
import akka.pattern.pipe
import io.netty.bootstrap.Bootstrap
import io.netty.channel.EventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel

import scala.concurrent.Promise

class ClientRequestActor(config: ClientConfig, group: EventLoopGroup) extends Actor {

  import context.dispatcher

  override def receive = {

    case HttpRequest.Outgoing(req, url, instructions) =>

      val port =
        if (url.getPort == -1) {
          if (url.getProtocol equalsIgnoreCase "https") 443 else 80
        } else {
          url.getPort
        }

      val promise = Promise[HttpResponse]
      new Bootstrap()
        .group(group)
        .channel(classOf[NioSocketChannel])
        .handler(new HttpChannelInitializer(config, url, req, promise))
        .connect(url.getHost, port)
        .onFailure(promise failure _.cause)
      promise.future andThen PartialFunction(_ => self ! PoisonPill) pipeTo sender
  }
}

object ClientRequestActor {

  def props(config: ClientConfig, group: EventLoopGroup): Props = Props(new ClientRequestActor(config, group))
}
