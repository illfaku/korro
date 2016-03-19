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
package org.oxydev.korro.http.internal.client.actor

import org.oxydev.korro.http.api.{HttpRequest, OutgoingHttpRequest}
import org.oxydev.korro.http.internal.client.config.ClientConfig
import org.oxydev.korro.util.concurrent.SequenceThreadFactory

import akka.actor._
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class HttpClientActor(config: ClientConfig) extends Actor with ActorLogging {

  private var group: EventLoopGroup = null

  override val supervisorStrategy = SupervisorStrategy.stoppingStrategy

  override def preStart(): Unit = {
    group = new NioEventLoopGroup(config.workerGroupSize, new SequenceThreadFactory(s"korro-client-${config.name}"))
    log.info("Started Korro HTTP client \"{}\" with URL: {}.", config.name, config.url)
  }

  override def postStop(): Unit = {
    if (group != null) group.shutdownGracefully()
  }

  override def receive = {

    case req: HttpRequest => config.url match {
      case Some(url) => self forward (req to url)
      case None => sender ! Status.Failure(new IllegalStateException("URL is not configured."))
    }

    case outgoing: OutgoingHttpRequest => HttpRequestActor.create(config, group) forward outgoing
  }
}

object HttpClientActor {

  def create(config: ClientConfig)(implicit factory: ActorRefFactory): ActorRef = {
    factory.actorOf(props(config), config.name)
  }

  def props(config: ClientConfig): Props = Props(new HttpClientActor(config))
}
