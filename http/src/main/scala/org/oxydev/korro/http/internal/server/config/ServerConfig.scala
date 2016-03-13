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

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class ServerConfig(val name: String, config: Config) {
  val port = config.findInt("port").getOrElse(8080)
  val workerGroupSize = config.findInt("worker-group-size").getOrElse(1)
  val http = config.findConfig("HTTP").map(new StandardHttpConfig(_)).getOrElse(DefaultHttpConfig)
  val ws = config.findConfig("WebSocket").map(new StandardWsConfig(_)).getOrElse(DefaultWsConfig)
}
