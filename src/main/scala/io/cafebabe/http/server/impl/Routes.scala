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

import io.cafebabe.util.config.wrapped

import com.typesafe.config.Config

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class Routes(config: Config) {

  def resolveTimeout: FiniteDuration = config.findFiniteDuration("timeout.resolve") getOrElse (10 seconds)

  def requestTimeout: FiniteDuration = config.findFiniteDuration("timeout.request") getOrElse (60 seconds)

  def apply(path: String): Route = httpRoute(path) orElse wsRoute(path) getOrElse NoRoute

  private def httpRoute(path: String): Option[HttpRoute] = {
    config.findConfigList("HTTP")
      .find(route => path startsWith route.getString("path"))
      .map(route => HttpRoute(route.getString("path"), route.getString("actor")))
  }

  private def wsRoute(path: String): Option[WsRoute] = {
    config.findConfigList("WebSocket")
      .find(route => path == route.getString("path"))
      .map(route => WsRoute(route.getString("path"), route.getString("actor")))
  }
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
sealed trait Route

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
case class HttpRoute(path: String, actor: String) extends Route

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
case class WsRoute(path: String, actor: String) extends Route

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
case object NoRoute extends Route
