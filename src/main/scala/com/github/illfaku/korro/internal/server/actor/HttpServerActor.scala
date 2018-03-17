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
package com.github.illfaku.korro.internal.server.actor

import org.oxydev.korro.api.config.ServerConfig
import org.oxydev.korro.internal.common.ChannelFutureExt
import com.github.illfaku.korro.internal.server.Keys
import com.github.illfaku.korro.internal.server.handler.HttpChannelInitializer
import com.github.illfaku.korro.internal.server.route.HttpRequestRouter
import com.github.illfaku.korro.util.concurrent.SequenceThreadFactory

import akka.actor._
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{Channel, EventLoopGroup}

import scala.util.{Failure, Success}

class HttpServerActor extends FSM[HttpServerActor.State, HttpServerActor.Data] {

  import HttpServerActor._

  startWith(Starting, NoData)

  when (Starting) {

    case Event(config: ServerConfig, NoData) =>

      val bossGroup = new NioEventLoopGroup(1, new SequenceThreadFactory(s"korro-server-${config.name}-boss"))
      val workerGroup = new NioEventLoopGroup(
        config.workerGroupSize, new SequenceThreadFactory(s"korro-server-${config.name}-worker")
      )

      val router = new HttpRequestRouter(config)
      HttpRequestRouterActor.create(router)

      val bootstrap = new ServerBootstrap()
        .group(bossGroup, workerGroup)
        .channel(classOf[NioServerSocketChannel])
        .childHandler(new HttpChannelInitializer(config))
        .childAttr(Keys.config, config)
        .childAttr(Keys.router, router)
        .childAttr(Keys.reqParent, HttpRequestParentActor.create())

      bootstrap.bind(config.port) onComplete {
        case Success(channel) => self ! channel
        case Failure(cause) => self ! Status.Failure(cause)
      }

      goto (Binding) using ConfigData(config, bossGroup, workerGroup)
  }

  when (Binding) {

    case Event(channel: Channel, ConfigData(config, boss, worker)) =>
      log.debug("Started Korro HTTP server \"{}\" on port {}.", config.name, config.port)
      goto (Working) using ChannelData(config, boss, worker, channel)

    case Event(Status.Failure(cause), ConfigData(config, boss, worker)) =>
      log.error(cause, "Failed to start Korro HTTP server \"{}\" on port {}.", config.name, config.port)
      stop(FSM.Failure(cause))
  }

  when (Working) (FSM.NullFunction)

  onTermination {

    case StopEvent(FSM.Normal, Working, ChannelData(config, boss, worker, channel)) =>
      channel.close()
      boss.shutdownGracefully()
      worker.shutdownGracefully()
      log.debug("Stopped Korro HTTP server \"{}\" on port {}.", config.name, config.port)

    case StopEvent(FSM.Failure(cause), Binding, ConfigData(config, boss, worker)) =>
      boss.shutdownGracefully()
      worker.shutdownGracefully()
  }

  initialize()
}

object HttpServerActor {

  sealed trait State
  case object Starting extends State
  case object Binding extends State
  case object Working extends State

  sealed trait Data
  case object NoData extends Data
  case class ConfigData(config: ServerConfig, boss: EventLoopGroup, worker: EventLoopGroup) extends Data
  case class ChannelData(config: ServerConfig, boss: EventLoopGroup, worker: EventLoopGroup, channel: Channel) extends Data


  def create(name: String)(implicit factory: ActorRefFactory): ActorRef = factory.actorOf(Props[HttpServerActor], name)
}
