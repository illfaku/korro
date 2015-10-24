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

import io.cafebabe.korro.server.handler.HttpChannelInitializer
import io.cafebabe.korro.util.akka.NoReceiveActor
import io.cafebabe.korro.util.concurrent.IncrementalThreadFactory
import io.cafebabe.korro.util.config.wrapped

import akka.actor._
import com.typesafe.config.Config
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{Channel, EventLoopGroup}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object HttpServerActor {
  def props(name: String, config: Config): Props = Props(new HttpServerActor(name, config))
}

class HttpServerActor(name: String, config: Config) extends Actor with ActorLogging with NoReceiveActor {

  private val port = config.findInt("port").getOrElse(8080)
  private val workerGroupSize = config.findInt("workerGroupSize").getOrElse(1)

  private var bossGroup: EventLoopGroup = null
  private var workerGroup: EventLoopGroup = null
  private var channel: Channel = null

  override def preStart(): Unit = {
    try {
      bossGroup = new NioEventLoopGroup(1, new IncrementalThreadFactory(s"korro-server-$name-boss"))
      workerGroup = new NioEventLoopGroup(workerGroupSize, new IncrementalThreadFactory(s"korro-server-$name-worker"))

      val bootstrap = new ServerBootstrap()
        .group(bossGroup, workerGroup)
        .channel(classOf[NioServerSocketChannel])
        .childHandler(new HttpChannelInitializer(config))

      channel = bootstrap.bind(port).sync().channel

      HttpRouterActor.create(config)

      log.info("Started Korro HTTP server \"{}\" on port {}.", name, port)
    } catch {
      case e: Throwable =>
        log.error(e, "Failed to start Korro HTTP server \"{}\" on port {}.", name, port)
        context.stop(self)
    }
  }

  override def postStop(): Unit = {
    if (channel != null) channel.close()
    if (bossGroup != null) bossGroup.shutdownGracefully()
    if (workerGroup != null) workerGroup.shutdownGracefully()
  }
}
