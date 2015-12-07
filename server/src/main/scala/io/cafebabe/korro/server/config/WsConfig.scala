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

import io.cafebabe.korro.api.ws.WsProtocol
import io.cafebabe.korro.internal.ws.JsonRpcWsProtocol
import io.cafebabe.korro.util.config.wrapped

import com.typesafe.config.Config

import scala.util.Try

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
trait WsConfig {
  def maxFramePayloadLength: Int
  def compression: Boolean
  def protocol: Option[WsProtocol]
  def routes: RoutesConfig
}

class StandardWsConfig(config: Config) extends WsConfig {
  override val maxFramePayloadLength: Int = config.findBytes("maxFramePayloadLength").map(_.toInt).getOrElse(DefaultWsConfig.maxFramePayloadLength)
  override val compression: Boolean = config.findBoolean("compression").getOrElse(DefaultWsConfig.compression)
  override val protocol: Option[WsProtocol] = config.findString("protocol") flatMap {
    case "json-rpc" => Some(JsonRpcWsProtocol)
    case className => Try(Class.forName(className)).filter(classOf[WsProtocol].isAssignableFrom).map(_.newInstance.asInstanceOf[WsProtocol]).toOption
  }
  override val routes: RoutesConfig = RoutesConfig(config.findConfigList("routes"))
}

object DefaultWsConfig extends WsConfig {
  override val maxFramePayloadLength: Int = 65536
  override val compression: Boolean = false
  override val protocol: Option[WsProtocol] = None
  override val routes: RoutesConfig = RoutesConfig(Nil)
}
