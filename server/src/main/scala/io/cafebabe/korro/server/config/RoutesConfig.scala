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
package io.cafebabe.korro.server.config

import io.cafebabe.korro.util.config.wrapped
import io.cafebabe.korro.util.log.Logging

import akka.actor.ActorPath
import com.typesafe.config.Config
import io.netty.handler.codec.http.HttpRequest

import java.util.regex.Pattern

import scala.util.Try

/**
  * TODO: Add description.
  *
  * @author Vladimir Konstantinov
  */
object RoutesConfig extends Logging {
  def apply(configs: Iterable[Config]): RoutesConfig = {
    val routes = for {
      route <- configs if route.hasPath("actor")
      actor <- Try(ActorPath.fromString(route.getString("actor"))).toOption
    } yield new RouteConfig(actor, route)
    new RoutesConfig(routes.toList)
  }
}

class RoutesConfig(routes: List[RouteConfig]) {
  def apply(req: HttpRequest): Option[ActorPath] = {
    def find(tail: List[RouteConfig]): Option[ActorPath] = tail match {
      case x :: xs => if (x.test(req)) Some(x.actor) else find(xs)
      case Nil => None
    }
    find(routes)
  }
}

private class RouteConfig(val actor: ActorPath, config: Config) {

  private val uriPattern: Option[Pattern] = config.findString("uri-pattern").map(Pattern.compile)

  def test(req: HttpRequest): Boolean = testUri(req.getUri)

  private def testUri(uri: String): Boolean = uriPattern match {
    case Some(pattern) => pattern.matcher(uri).matches()
    case None => true
  }
}
