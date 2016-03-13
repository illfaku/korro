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

import org.oxydev.korro.http.internal.server.config.ServerConfig
import org.oxydev.korro.http.internal.server.handler.HttpChannelInitializer
import org.oxydev.korro.util.concurrent.IncrementalThreadFactory

import akka.actor._
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{Channel, EventLoopGroup}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class HttpServerActor(config: ServerConfig) extends Actor with ActorLogging {

  private var bossGroup: EventLoopGroup = null
  private var workerGroup: EventLoopGroup = null
  private var channel: Channel = null

  override def preStart(): Unit = {
    try {
      bossGroup = new NioEventLoopGroup(1, new IncrementalThreadFactory(s"korro-server-${config.name}-boss"))
      workerGroup = new NioEventLoopGroup(config.workerGroupSize, new IncrementalThreadFactory(s"korro-server-${config.name}-worker"))

      val bootstrap = new ServerBootstrap()
        .group(bossGroup, workerGroup)
        .channel(classOf[NioServerSocketChannel])
        .childHandler(new HttpChannelInitializer(config, self))

      channel = bootstrap.bind(config.port).sync().channel

      log.info("Started Korro HTTP server \"{}\" on port {}.", config.name, config.port)
    } catch {
      case e: Throwable =>
        log.error(e, "Failed to start Korro HTTP server \"{}\" on port {}.", config.name, config.port)
        context.stop(self)
    }
  }

  override def postStop(): Unit = {
    if (channel != null) channel.close()
    if (bossGroup != null) bossGroup.shutdownGracefully()
    if (workerGroup != null) workerGroup.shutdownGracefully()
  }

  override def receive = {
    case HttpServerActor.CreateChild(props, true) => sender ! context.actorOf(props)
    case HttpServerActor.CreateChild(props, false) => context.actorOf(props)
  }
}

object HttpServerActor {

  def create(config: ServerConfig)(implicit factory: ActorRefFactory): ActorRef = {
    factory.actorOf(props(config), config.name)
  }

  def props(config: ServerConfig): Props = Props(new HttpServerActor(config))

  case class CreateChild(props: Props, returnRef: Boolean)
}
