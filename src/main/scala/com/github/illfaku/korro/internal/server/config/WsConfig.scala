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
package com.github.illfaku.korro.internal.server.config

import com.github.illfaku.korro.util.config.extended

import com.typesafe.config.Config

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
trait WsConfig {
  def maxFramePayloadLength: Int
  def compression: Boolean
  def decompression: Boolean
  def routes: RoutesConfig
  def logger: String
  def sourceIpHeader: Option[String]
}

class StandardWsConfig(config: Config) extends WsConfig {
  override val maxFramePayloadLength = config.findBytes("max-frame-payload-length").map(_.toInt).getOrElse(DefaultWsConfig.maxFramePayloadLength)
  override val compression = config.findBoolean("compression").getOrElse(DefaultWsConfig.compression)
  override val decompression = config.findBoolean("decompression").getOrElse(compression)
  override val routes = RoutesConfig(config.findConfigList("routes"))
  override val logger = config.findString("logger").getOrElse(DefaultWsConfig.logger)
  override val sourceIpHeader = config.findString("source-ip-header")
}

object DefaultWsConfig extends WsConfig {
  override val maxFramePayloadLength = 65536
  override val compression = false
  override val decompression = compression
  override val routes = RoutesConfig(Nil)
  override val logger = "korro-ws"
  override val sourceIpHeader = None
}
