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
