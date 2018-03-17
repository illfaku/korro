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

import org.oxydev.korro.api.route.{SetRoute, UnsetRoute}
import com.github.illfaku.korro.internal.server.route.HttpRequestRouter

import akka.actor.{Actor, ActorRef, ActorRefFactory, Props, Terminated}

/**
 * Internal router actor for HTTP requests.
 *
 * <p>Accepts [[org.oxydev.korro.api.route.SetRoute SetRoute]] and
 * [[org.oxydev.korro.api.route.UnsetRoute UnsetRoute]] commands.
 */
class HttpRequestRouterActor(router: HttpRequestRouter) extends Actor {

  override def receive = {

    case SetRoute(ref, predicate, instructions) =>
      router.set(ref, predicate, instructions)
      context watch ref

    case UnsetRoute(ref) =>
      router.unset(ref)
      context unwatch ref

    case Terminated(ref) =>
      router.unset(ref)
      context unwatch ref
  }
}

object HttpRequestRouterActor {

  def create(router: HttpRequestRouter)(implicit factory: ActorRefFactory): ActorRef = {
    factory.actorOf(props(router), "router")
  }

  def props(router: HttpRequestRouter): Props = Props(new HttpRequestRouterActor(router))
}
