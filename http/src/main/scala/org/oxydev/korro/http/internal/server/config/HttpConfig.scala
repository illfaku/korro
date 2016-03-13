/*
 * Copyright 2016 Vladimir Konstantinov, Yuriy Gintsyak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
