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
package org.oxydev.korro.http.client.actor

import org.oxydev.korro.http.api.HttpRequest
import org.oxydev.korro.http.client.config.ClientConfig
import org.oxydev.korro.util.concurrent.IncrementalThreadFactory

import akka.actor._
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup

import java.net.{URI, URL}

import scala.util.{Failure, Success, Try}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class HttpClientActor(config: ClientConfig) extends Actor with ActorLogging {

  private var group: EventLoopGroup = null

  override val supervisorStrategy = SupervisorStrategy.stoppingStrategy

  override def preStart(): Unit = {
    group = new NioEventLoopGroup(config.workerGroupSize, new IncrementalThreadFactory(s"korro-client-${config.name}"))
    log.info("Started Korro HTTP client \"{}\" with URL: {}.", config.name, config.url)
  }

  override def postStop(): Unit = {
    if (group != null) group.shutdownGracefully()
  }

  override def receive = {

    case req: HttpRequest => config.url match {
      case Some(url) => self forward (url -> req)
      case None => sender ! Status.Failure(new IllegalStateException("URL is not configured."))
    }

    case (uri: URI, req: HttpRequest) => Try (uri.toURL) match {
      case Success(url) => self forward (url -> req)
      case Failure(err) => sender ! Status.Failure(err)
    }

    case (url: URL, req: HttpRequest) =>
      val request = req.copy(headers = req.headers + ("Host" -> url.getHost))
      HttpRequestActor.create(config, group) forward (url -> request)
  }
}

object HttpClientActor {

  def create(config: ClientConfig)(implicit factory: ActorRefFactory): ActorRef = {
    factory.actorOf(props(config), config.name)
  }

  def props(config: ClientConfig): Props = Props(new HttpClientActor(config))
}
