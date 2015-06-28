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
package io.cafebabe.http.server.impl.handler

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

  private val httpResolveTimeout: FiniteDuration = {
    config.findFiniteDuration("HTTP.resolveTimeout") getOrElse 10.seconds
  }

  private val httpRequestTimeout: FiniteDuration = {
    config.findFiniteDuration("HTTP.requestTimeout") getOrElse 60.seconds
  }

  private val wsResolveTimeout: FiniteDuration = {
    config.findFiniteDuration("WebSocket.resolveTimeout") getOrElse 10.seconds
  }

  private val maxFramePayloadLength: Int = {
    config.findBytes("WebSocket.maxFramePayloadLength").getOrElse(65536L).toInt
  }

  private val httpRoutes: List[HttpRoute] = config.findConfigList("HTTP.routes").toList.map { route =>
    HttpRoute(route.getString("path"), httpResolveTimeout, httpRequestTimeout, route.getString("actor"))
  }

  private val wsRoutes: List[WsRoute] = config.findConfigList("WebSocket.routes").toList.map { route =>
    WsRoute(route.getString("path"), wsResolveTimeout, maxFramePayloadLength, route.getString("actor"))
  }

  def apply(path: String): Route = httpRoute(path) orElse wsRoute(path) getOrElse NoRoute

  private def httpRoute(path: String): Option[HttpRoute] = httpRoutes.find(path startsWith _.path)

  private def wsRoute(path: String): Option[WsRoute] = wsRoutes.find(path == _.path)
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
case class HttpRoute(
  path: String,
  resolveTimeout: FiniteDuration,
  requestTimeout: FiniteDuration,
  actor: String
) extends Route

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
case class WsRoute(
  path: String,
  resolveTimeout: FiniteDuration,
  maxFramePayloadLength: Int,
  actor: String
) extends Route

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
case object NoRoute extends Route
