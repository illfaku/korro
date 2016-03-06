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

import scala.concurrent.duration._

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
sealed trait HttpConfig {
  def maxContentLength: Long
  def minContentLength: Long
  def compressionLevel: Option[Int]
  def requestTimeout: FiniteDuration
  def routes: RoutesConfig
  def logger: String
}

class StandardHttpConfig(config: Config) extends HttpConfig {
  override val maxContentLength = config.findBytes("max-content-length").getOrElse(DefaultHttpConfig.maxContentLength)
  override val minContentLength = config.findBytes("min-content-length").getOrElse(maxContentLength)
  override val compressionLevel = config.findInt("compression")
  override val requestTimeout = config.findFiniteDuration("request-timeout").getOrElse(DefaultHttpConfig.requestTimeout)
  override val routes = RoutesConfig(config.findConfigList("routes"))
  override val logger = config.findString("logger").getOrElse(DefaultHttpConfig.logger)
}

object DefaultHttpConfig extends HttpConfig {
  override val maxContentLength = 65536L
  override val minContentLength = maxContentLength
  override val compressionLevel = None
  override val requestTimeout = 60 seconds
  override val routes = RoutesConfig(Nil)
  override val logger = "korro-server"
}
