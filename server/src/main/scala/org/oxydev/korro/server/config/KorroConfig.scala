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
package org.oxydev.korro.server.config

import org.oxydev.korro.util.config.wrapped

import com.typesafe.config.Config

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class KorroConfig(val name: String, config: Config) {
  val port = config.findInt("port").getOrElse(8080)
  val workerGroupSize = config.findInt("workerGroupSize").getOrElse(1)
  val http = config.findConfig("HTTP").map(new StandardHttpConfig(_)).getOrElse(DefaultHttpConfig)
  val ws = config.findConfig("WebSocket").map(new StandardWsConfig(_)).getOrElse(DefaultWsConfig)
}
