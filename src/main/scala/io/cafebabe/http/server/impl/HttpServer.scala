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
package io.cafebabe.http.server.impl

import io.cafebabe.http.server.impl.handler.HttpChannelInitializer
import io.cafebabe.http.server.impl.util.IncrementalThreadFactory
import io.cafebabe.util.config.wrapped

import akka.actor.ActorSystem
import com.typesafe.config.Config
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{Channel, EventLoopGroup}
import io.netty.handler.logging.{LogLevel, LoggingHandler}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object HttpServer {

  def apply(config: Config, actors: ActorSystem): HttpServer = {

    val port = config.getInt("port")
    val workerGroupSize = config.findInt("workerGroupSize").getOrElse(1)

    val bossGroup = new NioEventLoopGroup(1, new IncrementalThreadFactory(s"http-$port-boss"))
    val workerGroup = new NioEventLoopGroup(workerGroupSize, new IncrementalThreadFactory(s"http-$port-worker"))

    val bootstrap = new ServerBootstrap()
      .group(bossGroup, workerGroup)
      .channel(classOf[NioServerSocketChannel])
      .handler(new LoggingHandler(LogLevel.DEBUG))
      .childHandler(new HttpChannelInitializer(config, actors))

    val channel = bootstrap.bind(port).sync().channel

    new HttpServer(port, channel, bossGroup, workerGroup)
  }
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class HttpServer(val port: Int, channel: Channel, bossGroup: EventLoopGroup, workerGroup: EventLoopGroup) {

  def stop(): Unit = {
    channel.close().sync()
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }
}
