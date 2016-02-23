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
package org.oxydev.korro.client.actor

import org.oxydev.korro.api.http.{HttpRequest, HttpResponse}
import org.oxydev.korro.client.handler.HttpChannelInitializer
import org.oxydev.korro.internal.ChannelFutureExt

import akka.actor._
import akka.pattern.pipe
import io.netty.bootstrap.Bootstrap
import io.netty.channel.EventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel

import java.net.URL

import scala.concurrent.Promise
import scala.concurrent.duration.FiniteDuration

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class HttpRequestActor(group: EventLoopGroup, timeout: FiniteDuration) extends Actor {

  import context.dispatcher

  override def receive = {
    case (url: URL, req: HttpRequest) =>
      val promise = Promise[HttpResponse]
      new Bootstrap()
        .group(group)
        .channel(classOf[NioSocketChannel])
        .handler(new HttpChannelInitializer(url, req, promise, timeout))
        .connect(url.getHost, url.getPort)
        .foreach { f => if (!f.isSuccess) promise.failure(f.cause) }
      promise.future andThen PartialFunction(_ => self ! PoisonPill) pipeTo sender
  }
}

object HttpRequestActor {

  def create(group: EventLoopGroup, timeout: FiniteDuration)(implicit factory: ActorRefFactory): ActorRef = {
    factory.actorOf(props(group, timeout))
  }

  def props(group: EventLoopGroup, timeout: FiniteDuration): Props = Props(new HttpRequestActor(group, timeout))
}
