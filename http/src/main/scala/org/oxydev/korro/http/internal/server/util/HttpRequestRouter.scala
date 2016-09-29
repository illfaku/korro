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
package org.oxydev.korro.http.internal.server.util

import org.oxydev.korro.http.api.HttpRequest
import org.oxydev.korro.http.api.route.RoutePredicate
import org.oxydev.korro.util.lang.Predicate1

import akka.actor.ActorRef

object HttpRequestRouter {

  case class Route(ref: ActorRef)

  private [HttpRequestRouter] case class RouteInfo(predicate: RoutePredicate)
}

class HttpRequestRouter {

  import HttpRequestRouter._

  @volatile
  private var routes = Map.empty[ActorRef, RouteInfo]

  def set(ref: ActorRef, predicate: RoutePredicate): Unit = routes = routes + (ref -> RouteInfo(predicate))

  def unset(ref: ActorRef): Unit = routes = routes - ref

  /**
   * Returns optional matching route.
   *
   * @param req Request to match.
   */
  def find(req: HttpRequest): Option[Route] = routes.find(r => check(req, r._2.predicate)).map(_._1).map(Route)

  private def check(req: HttpRequest, predicate: RoutePredicate): Boolean = predicate match {
    case RoutePredicate.MethodIs(method) => req.method == method
    case RoutePredicate.PathIs(path) => req.path == path
    case RoutePredicate.PathStartsWith(prefix) => req.path startsWith prefix
    case RoutePredicate.PathEndsWith(suffix) => req.path endsWith suffix
    case RoutePredicate.PathMatch(regexp) => regexp.r.findFirstIn(req.path).isDefined
    case RoutePredicate.HasQueryParam(name) => req.parameters.contains(name)
    case RoutePredicate.HasQueryParamValue(name, value) => req.parameters.contains(name, value)
    case RoutePredicate.HasHeader(name) => req.headers.contains(name)
    case RoutePredicate.HasHeaderValue(name, value) => req.headers.contains(name, value)
    case RoutePredicate.Or(a, b) => check(req, a) || check(req, b)
    case RoutePredicate.And(a, b) => check(req, a) && check(req, b)
  }
}
