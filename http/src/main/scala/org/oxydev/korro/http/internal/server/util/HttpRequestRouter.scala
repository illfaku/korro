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
import org.oxydev.korro.util.lang.Predicate1

import akka.actor.ActorRef

import scala.collection.mutable

object HttpRequestRouter {

  case class Route(ref: ActorRef)

  private [HttpRequestRouter] case class RouteInfo(predicate: Predicate1[HttpRequest])
}

class HttpRequestRouter {

  import HttpRequestRouter._

  private val routes = mutable.Map.empty[ActorRef, RouteInfo]

  def set(ref: ActorRef, predicate: Predicate1[HttpRequest]): Unit = routes += (ref -> RouteInfo(predicate))

  def unset(ref: ActorRef): Unit = routes -= ref

  /**
   * Returns optional matching route.
   *
   * @param req Request to match.
   */
  def find(req: HttpRequest): Option[Route] = routes.find(_._2.predicate(req)).map(_._1).map(Route)
}
