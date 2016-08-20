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

import org.oxydev.korro.http.api.HttpRequest
import org.oxydev.korro.http.internal.server.config.HttpConfig

import akka.actor.{Actor, ActorRef, ActorRefFactory, Props}
import io.netty.channel.Channel

class HttpRequestParentActor(config: HttpConfig) extends Actor {

  override def receive = {
    case HttpRequestParentActor.NewRequest(channel, route, req) =>
      implicit val child = context.actorOf(HttpRequestActor.props(channel, config, s"${req.method} ${req.path}"))
      route ! req
  }
}

object HttpRequestParentActor {

  def create(config: HttpConfig)(implicit factory: ActorRefFactory): ActorRef = {
    factory.actorOf(props(config), "request")
  }

  def props(config: HttpConfig): Props = Props(new HttpRequestParentActor(config))

  case class NewRequest(channel: Channel, route: ActorRef, req: HttpRequest)
}
