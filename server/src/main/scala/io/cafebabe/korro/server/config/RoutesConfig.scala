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
import io.netty.handler.codec.http.{HttpMethod, HttpRequest}

import java.util.regex.Pattern

import scala.util.Try

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object RoutesConfig extends Logging {
  def apply(configs: Iterable[Config]): RoutesConfig = {
    val routes = configs.filter(_.hasPath("actor")).map(new RouteConfig(_))
    new RoutesConfig(routes.toList)
  }
}

/**
  * TODO: Add description.
  *
  * @author Vladimir Konstantinov
  */
class RoutesConfig(routes: List[RouteConfig]) {
  def apply(req: HttpRequest): Option[String] = {
    def find(tail: List[RouteConfig]): Option[String] = tail match {
      case x :: xs => if (x.test(req)) Some(x.actor) else find(xs)
      case Nil => None
    }
    find(routes)
  }
}

/**
  * TODO: Add description.
  *
  * @author Vladimir Konstantinov
  */
private class RouteConfig(config: Config) {

  val actor = config.getString("actor")

  private val method: Option[String] = config.findString("method")

  private val path: Option[String] = config.findString("path")

  private val uriPattern: Option[Pattern] = config.findString("uri-pattern").map(Pattern.compile)

  def test(req: HttpRequest): Boolean = testMethod(req.getMethod) && testPath(req.getUri) && testUri(req.getUri)

  private def testMethod(m: HttpMethod): Boolean = method.map(_ equalsIgnoreCase m.name).getOrElse(true)

  private def testPath(uri: String): Boolean = path.map(uri.startsWith).getOrElse(true)

  private def testUri(uri: String): Boolean = uriPattern.map(_.matcher(uri).matches).getOrElse(true)
}
