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
package org.oxydev.korro.client.config

import org.oxydev.korro.util.config.wrapped

import com.typesafe.config.Config

import java.net.URL

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class ClientConfig(val name: String, config: Config) {
  val url: Option[URL] = config.findURL("url")
  val workerGroupSize: Int = config.findInt("workerGroupSize").getOrElse(1)
}
