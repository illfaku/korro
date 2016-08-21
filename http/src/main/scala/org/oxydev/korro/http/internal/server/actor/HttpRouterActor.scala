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

import org.oxydev.korro.http.tools.route.HttpRouter.{SetRoute, UnsetRoute}

import akka.actor.{Actor, ActorRef, ActorRefFactory, Props, Terminated}

class HttpRouterActor(router: HttpRouter) extends Actor {

  override def receive = {

    case SetRoute(ref, predicate) =>
      router.set(ref, predicate)
      context watch ref

    case UnsetRoute(ref) =>
      router.unset(ref)
      context unwatch ref

    case Terminated(ref) =>
      router.unset(ref)
      context unwatch ref
  }
}

object HttpRouterActor {

  def create(router: HttpRouter)(implicit factory: ActorRefFactory): ActorRef = factory.actorOf(props(router), "router")

  def props(router: HttpRouter): Props = Props(new HttpRouterActor(router))
}
