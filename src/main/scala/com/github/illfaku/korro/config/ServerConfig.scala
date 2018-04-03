/*
 * Copyright 2018 Vladimir Konstantinov
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
package com.github.illfaku.korro.config

import com.github.illfaku.korro.util.configOptions

import com.typesafe.config.Config

case class ServerConfig(
  port: Int = ServerConfig.Defaults.port,
  nettyDispatcher: Option[String] = None,
  nettyThreads: Int = ServerConfig.Defaults.nettyThreads,
  nettyLogger: String = ServerConfig.Defaults.nettyLogger,
  instructions: List[HttpInstruction] = Nil,
  routes: List[RouteConfig] = Nil
)

object ServerConfig {

  object Defaults {
    val port: Int = 8080
    val nettyThreads: Int = 0
    val nettyLogger: String = "korro-netty"
  }

  def extract(config: Config): ServerConfig = {
    ServerConfig(
      config.findInt("port").getOrElse(Defaults.port),
      config.findString("netty-dispatcher"),
      config.findInt("netty-threads").getOrElse(Defaults.nettyThreads),
      config.findString("netty-logger").getOrElse(Defaults.nettyLogger),
      config.findConfig("instructions").map(HttpInstruction.extract).getOrElse(Nil),
      config.findConfigList("routes").flatMap(RouteConfig.extract)
    )
  }
}
