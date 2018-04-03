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

import java.net.URL

case class ClientConfig(
  url: Option[URL] = None,
  nettyDispatcher: Option[String] = None,
  nettyThreads: Int = ClientConfig.Defaults.nettyThreads,
  nettyLogger: String = ClientConfig.Defaults.nettyLogger,
  instructions: List[HttpInstruction] = Nil
)

object ClientConfig {

  object Defaults {
    val nettyThreads: Int = 1
    val nettyLogger: String = "korro-netty"
  }

  def extract(config: Config): ClientConfig = {
    ClientConfig(
      config.findURL("url"),
      config.findString("netty-dispatcher"),
      config.findInt("netty-threads").getOrElse(Defaults.nettyThreads),
      config.findString("netty-logger").getOrElse(Defaults.nettyLogger),
      config.findConfig("instructions").map(HttpInstruction.extract).getOrElse(Nil)
    )
  }
}
