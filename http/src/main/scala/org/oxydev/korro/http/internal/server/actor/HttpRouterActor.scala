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
import org.oxydev.korro.http.api.HttpResponse.Status
import org.oxydev.korro.http.api.route.{SetRoute, UnsetRoute}
import org.oxydev.korro.http.internal.server.actor.HttpRouter.Route

import akka.actor.{Actor, ActorRef, ActorRefFactory, Props, Terminated}

/**
 * Internal router actor for HTTP requests.
 * Accepts [[org.oxydev.korro.http.api.route.SetRoute SetRoute]] and
 * [[org.oxydev.korro.http.api.route.UnsetRoute UnsetRoute]] commands.
 *
 * <p>It tries to find actor with predicate that matches [[org.oxydev.korro.http.api.HttpRequest HttpRequest]] message
 * and forwards message to it, if not found it will send response with status 404 to sender.
 */
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

    case req: HttpRequest =>
      router.find(req) match {
        case Some(Route(ref)) => ref forward req
        case None => sender ! Status.NotFound()
      }
  }
}

object HttpRouterActor {

  def create(router: HttpRouter)(implicit factory: ActorRefFactory): ActorRef = factory.actorOf(props(router), "router")

  def props(router: HttpRouter): Props = Props(new HttpRouterActor(router))
}
