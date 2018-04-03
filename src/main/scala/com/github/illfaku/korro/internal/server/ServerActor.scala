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
package com.github.illfaku.korro.internal.server

import com.github.illfaku.korro.config.ServerConfig
import com.github.illfaku.korro.internal.common.{HttpActorFactory, HttpInstructions}

import akka.actor._
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{Channel, EventLoopGroup}

class ServerActor(config: ServerConfig) extends Actor with ActorLogging with HttpActorFactory with Routing {

  private val executor = config.nettyDispatcher.map(context.system.dispatchers.lookup).getOrElse(context.dispatcher)

  private val instructions = HttpInstructions().merge(config.instructions)

  private var group: EventLoopGroup = _

  private var channel: Channel = _

  override def preStart() = {
    super.preStart()

    config.routes.foreach(self.!)

    group = new NioEventLoopGroup(config.nettyThreads, executor)

    channel = new ServerBootstrap()
      .group(group)
      .channel(classOf[NioServerSocketChannel])
      .childHandler(new HttpChannelInitializer(self, config, instructions))
      .bind(config.port)
      .sync.channel

    log.debug("Korro | Started server on port {} at {}.", config.port, self.path.toStringWithoutAddress)
  }

  override def postStop() = {
    if (channel != null) channel.close().sync()
    if (group != null) group.shutdownGracefully().sync()
    log.debug("Korro | Stopped server on port {} at {}.", config.port, self.path.toStringWithoutAddress)
    super.postStop()
  }

  override def receive = httpActorCreation orElse routing(instructions)
}
