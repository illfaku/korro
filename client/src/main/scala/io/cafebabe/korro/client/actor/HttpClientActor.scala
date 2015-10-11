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
package io.cafebabe.korro.client.actor

import io.cafebabe.korro.api.http.{HttpResponse, HttpRequest}
import io.cafebabe.korro.client.KorroClientActor
import io.cafebabe.korro.client.handler.HttpChannelInitializer
import io.cafebabe.korro.util.config.wrapped

import akka.actor._
import akka.pattern.pipe
import com.typesafe.config.Config
import io.netty.bootstrap.Bootstrap
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel

import java.net.URI

import scala.concurrent.Promise

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object HttpClientActor {

  def path(name: String): String = s"${KorroClientActor.path}/$name"

  def create(config: Config)(implicit factory: ActorRefFactory): ActorRef = {
    factory.actorOf(Props(new HttpClientActor(config)), config.getString("name"))
  }
}

class HttpClientActor(config: Config) extends Actor {

  private val uriOption = config.findURI("uri")
  private val workerGroupSize = config.findInt("workerGroupSize").getOrElse(1)

  private var group: EventLoopGroup = null

  override def preStart(): Unit = {
    group = new NioEventLoopGroup(workerGroupSize)
  }

  override def postStop(): Unit = {
    if (group != null) group.shutdownGracefully()
  }

  import context.dispatcher

  override def receive = {

    case req: HttpRequest => uriOption match {
      case Some(uri) => self forward (uri -> req)
      case None => sender ! Status.Failure(new IllegalStateException("URI is not configured."))
    }

    case (uri: URI, req: HttpRequest) =>
      val promise = Promise[HttpResponse]()
      new Bootstrap()
        .group(group)
        .channel(classOf[NioSocketChannel])
        .handler(new HttpChannelInitializer(config, uri, req, promise))
        .connect(uri.getHost, uri.getPort)
      promise.future pipeTo sender
  }
}
