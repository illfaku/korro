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
package com.github.illfaku.korro.internal.client

import com.github.illfaku.korro.config.ClientConfig
import com.github.illfaku.korro.dto.HttpRequest
import com.github.illfaku.korro.dto.ws.WsHandshakeRequest
import com.github.illfaku.korro.internal.common.HttpActorFactory

import akka.actor._
import io.netty.bootstrap.Bootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.channel.{ChannelFuture, ChannelInitializer, EventLoopGroup}

import java.net.{URI, URL}

class ClientActor(config: ClientConfig) extends Actor with ActorLogging with HttpActorFactory {

  private val executor = config.nettyDispatcher.map(context.system.dispatchers.lookup).getOrElse(context.dispatcher)

  private var group: EventLoopGroup = _

  override def preStart(): Unit = {
    super.preStart()
    group = new NioEventLoopGroup(config.nettyThreads, executor)
    log.debug("Korro | Started client at {}.", self.path.toStringWithoutAddress)
  }

  override def postStop(): Unit = {
    group.shutdownGracefully()
    super.postStop()
  }

  override def receive = httpActorCreation orElse {

    case req: HttpRequest => config.url match {
      case Some(url) => self forward (req to url)
      case None => sender ! Status.Failure(new IllegalStateException("URL is not configured."))
    }

    case req: WsHandshakeRequest => config.url match {
      case Some(url) => self forward (req to url)
      case None => sender ! Status.Failure(new IllegalStateException("URL is not configured."))
    }

    case HttpRequest.Outgoing(req, url, instructions) =>
      val reqConfig = config.copy(url = Some(url), instructions = config.instructions ::: instructions)
      connect(url, new HttpChannelInitializer(reqConfig, req, sender))

    case WsHandshakeRequest.Outgoing(WsHandshakeRequest(inActor, uri, headers), url, instructions) =>
      val wsUri = new URI(url.getProtocol, null, url.getHost, url.getPort, uri.path, uri.query, null)
      val reqConfig = config.copy(url = Some(url), instructions = config.instructions ::: instructions)
      connect(url, new WsChannelInitializer(self, reqConfig, wsUri, headers, inActor))
  }

  private def connect(url: URL, initializer: ChannelInitializer[SocketChannel]): ChannelFuture = {
    new Bootstrap()
      .group(group)
      .channel(classOf[NioSocketChannel])
      .handler(initializer)
      .connect(url.getHost, getPort(url))
  }
}
