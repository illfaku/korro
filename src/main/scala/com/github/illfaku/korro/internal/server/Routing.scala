/*
 * Copyright 2018 Vladimir Konstantinov
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
package com.github.illfaku.korro.internal.server

import com.github.illfaku.korro.config.{RequestPredicate, RouteActorPath, RouteActorRef, RouteConfig}
import com.github.illfaku.korro.dto.HttpRequest.Uri
import com.github.illfaku.korro.internal.common.HttpInstructions
import akka.pattern.ask
import akka.actor.{Actor, ActorRef, ActorSelection}
import akka.util.Timeout
import io.netty.handler.codec.http.HttpRequest

import scala.concurrent.Future

private[server] trait Routing { this: Actor =>

  import Routing._

  private var routes = List.empty[RouteConfig]

  protected def routing(defaultInstructions: HttpInstructions): Receive = {

    case route: RouteConfig =>
      if (route.predicate == RequestPredicate.False) routes = routes.filter(_.actor == route.actor)
      else routes = routes :+ route

    case FindRoute(req) =>
      val route = routes.find(r => test(req, Uri(req.uri), r.predicate)) map {
        case RouteConfig(RouteActorRef(ref), _, instructions) =>
          ActorRefRoute(ref, defaultInstructions.merge(instructions))
        case RouteConfig(RouteActorPath(path), _, instructions) =>
          ActorSelectionRoute(context.actorSelection(path), defaultInstructions.merge(instructions))
      } getOrElse {
        NoRoute
      }
      sender ! route
  }

  private def test(req: HttpRequest, uri: Uri, predicate: RequestPredicate): Boolean = {
    predicate match {
      case RequestPredicate.True => true
      case RequestPredicate.False => false
      case RequestPredicate.Or(a, b) => test(req, uri, a) || test(req, uri, b)
      case RequestPredicate.And(a, b) => test(req, uri, a) && test(req, uri, b)
      case RequestPredicate.MethodIs(method) => req.method.name == method
      case RequestPredicate.PathIs(path) => uri.path == path
      case RequestPredicate.PathStartsWith(prefix) => uri.path startsWith prefix
      case RequestPredicate.PathEndsWith(suffix) => uri.path endsWith suffix
      case RequestPredicate.PathMatch(regexp) => uri.path matches regexp
      case RequestPredicate.HasQueryParam(name) => uri.params.contains(name)
      case RequestPredicate.HasQueryParamValue(name, value) => uri.params.contains(name, value, ignoreCase = true)
      case RequestPredicate.HasHeader(name) => req.headers.contains(name)
      case RequestPredicate.HasHeaderValue(name, value) => req.headers.contains(name, value, true)
    }
  }
}

private[server] object Routing {

  case class FindRoute(req: HttpRequest)

  sealed trait Route

  case object NoRoute extends Route

  sealed trait DstRoute extends Route {
    val instructions: HttpInstructions
    def !(msg: Any)(implicit sender: ActorRef): Unit
    def ?(msg: Any)(implicit timeout: Timeout): Future[Any]
  }

  case class ActorRefRoute(ref: ActorRef, instructions: HttpInstructions) extends DstRoute {
    override def !(msg: Any)(implicit sender: ActorRef): Unit = ref ! msg
    override def ?(msg: Any)(implicit timeout: Timeout): Future[Any] = ref ? msg
  }

  case class ActorSelectionRoute(sel: ActorSelection, instructions: HttpInstructions) extends DstRoute {
    override def !(msg: Any)(implicit sender: ActorRef): Unit = sel ! msg
    override def ?(msg: Any)(implicit timeout: Timeout): Future[Any] = sel ? msg
  }
}
