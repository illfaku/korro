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
package org.oxydev.korro.http.internal.client.config

import org.oxydev.korro.util.config.wrapped

import com.typesafe.config.Config

import java.net.URL

import scala.concurrent.duration._

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class ClientConfig(val name: String, config: Config) {
  val url: Option[URL] = config.findURL("url")
  val workerGroupSize: Int = config.findInt("worker-group-size").getOrElse(1)
  val requestTimeout: FiniteDuration = config.findFiniteDuration("request-timeout").getOrElse(60 seconds)
  val logger: String = config.findString("logger").getOrElse("korro-client")
}
