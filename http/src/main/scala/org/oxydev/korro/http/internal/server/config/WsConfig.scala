/*
 * Copyright (C) 2015, 2016  Vladimir Konstantinov, Yuriy Gintsyak
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
package org.oxydev.korro.http.internal.server.config

import org.oxydev.korro.util.config.wrapped

import com.typesafe.config.Config

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
trait WsConfig {
  def maxFramePayloadLength: Int
  def compression: Boolean
  def routes: RoutesConfig
  def logger: String
}

class StandardWsConfig(config: Config) extends WsConfig {
  override val maxFramePayloadLength = config.findBytes("max-frame-payload-length").map(_.toInt).getOrElse(DefaultWsConfig.maxFramePayloadLength)
  override val compression = config.findBoolean("compression").getOrElse(DefaultWsConfig.compression)
  override val routes = RoutesConfig(config.findConfigList("routes"))
  override val logger = config.findString("logger").getOrElse(DefaultWsConfig.logger)
}

object DefaultWsConfig extends WsConfig {
  override val maxFramePayloadLength = 65536
  override val compression = false
  override val routes = RoutesConfig(Nil)
  override val logger = "korro-ws"
}
