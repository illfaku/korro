/*
 * Copyright (C) 2015  Vladimir Konstantinov, Yuriy Gintsyak
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
package io.cafebabe.http.server.impl

import akka.actor.ActorPath

/**
 * @author Vladimir Konstantinov
 * @version 1.0 (6/10/2015)
 */
sealed trait HttpRoute

case class RestRoute(uriPath: String, actorPath: ActorPath) extends HttpRoute

case class WsRoute(uriPath: String, actorPath: ActorPath, maxFramePayloadLength: Int) extends HttpRoute

case object NoRoute extends HttpRoute

class HttpRoutes(restRoutes: List[RestRoute], wsRoutes: List[WsRoute]) {
  def apply(path: String): HttpRoute = {
    restRoutes.find(route => path.startsWith(route.uriPath))
      .orElse(wsRoutes.find(_.uriPath == path))
      .getOrElse(NoRoute)
  }
}
