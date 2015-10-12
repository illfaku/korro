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

import io.cafebabe.korro.server.KorroServerActor
import io.cafebabe.korro.server.handler.HttpChannelInitializer
import io.cafebabe.korro.util.concurrent.IncrementalThreadFactory
import io.cafebabe.korro.util.config.wrapped

import akka.actor._
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
object HttpServerActor {

  def path(name: String): String = s"${KorroServerActor.path}/$name"

  def create(name: String, config: Config)(implicit factory: ActorRefFactory): ActorRef = {
    factory.actorOf(Props(new HttpServerActor(name, config)), name)
  }
}

class HttpServerActor(name: String, config: Config) extends Actor with ActorLogging {

  private var bossGroup: EventLoopGroup = null
  private var workerGroup: EventLoopGroup = null
  private var channel: Channel = null

  override def preStart(): Unit = {
    try {
      val port = config.findInt("port").getOrElse(8080)
      val workerGroupSize = config.findInt("workerGroupSize").getOrElse(1)

      bossGroup = new NioEventLoopGroup(1, new IncrementalThreadFactory(s"korro-server-$name-boss"))
      workerGroup = new NioEventLoopGroup(workerGroupSize, new IncrementalThreadFactory(s"korro-server-$name-worker"))

      val bootstrap = new ServerBootstrap()
        .group(bossGroup, workerGroup)
        .channel(classOf[NioServerSocketChannel])
        .handler(new LoggingHandler(LogLevel.DEBUG))
        .childHandler(new HttpChannelInitializer(config))

      channel = bootstrap.bind(port).sync().channel

      HttpRouterActor.create(config)

      log.debug("Started HTTP server on port {}.", port)
    } catch {
      case e: Throwable => log.error(e, s"Failed to start HTTP server.")
    }
  }

  override def postStop(): Unit = {
    try {
      if (channel != null) channel.close().sync()
      if (bossGroup != null) bossGroup.shutdownGracefully()
      if (workerGroup != null) workerGroup.shutdownGracefully()
      log.debug("Stopped HTTP server.")
    } catch {
      case e: Throwable => log.error(e, s"Failed to stop HTTP server.")
    }
  }

  override def receive = {
    case _ => ()
  }
}
