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
}

class StandardHttpConfig(config: Config) extends HttpConfig {
  override val maxContentLength: Long = config.findBytes("maxContentLength").getOrElse(DefaultHttpConfig.maxContentLength)
  override val minContentLength: Long = config.findBytes("minContentLength").getOrElse(maxContentLength)
  override val compressionLevel: Option[Int] = config.findInt("compression")
  override val requestTimeout: FiniteDuration = config.findFiniteDuration("requestTimeout").getOrElse(DefaultHttpConfig.requestTimeout)
  override val routes: RoutesConfig = RoutesConfig(config.findConfigList("routes"))
}

object DefaultHttpConfig extends HttpConfig {
  override val maxContentLength: Long = 65536L
  override val minContentLength: Long = maxContentLength
  override val compressionLevel: Option[Int] = None
  override val requestTimeout: FiniteDuration = 60 seconds
  override val routes: RoutesConfig = RoutesConfig(Nil)
}
