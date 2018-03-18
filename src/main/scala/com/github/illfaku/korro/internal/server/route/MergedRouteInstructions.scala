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
package com.github.illfaku.korro.internal.server.route

import org.oxydev.korro.api.config.ServerConfig

import scala.concurrent.duration._

case class MergedRouteInstructions(
  requestTimeout: FiniteDuration,
  maxContentLength: Long,
  contentAsFile: Boolean,
  fileContentRemoveDelay: Duration,
  responseCompression: Boolean,
  responseCompressionLevel: Int,
  maxWsFramePayloadLength: Int,
  wsLogger: String,
  simpleWsLogging: Boolean
)

object MergedRouteInstructions {

  val default = MergedRouteInstructions(
    requestTimeout = 5 seconds,
    maxContentLength = 65536,
    contentAsFile = false,
    fileContentRemoveDelay = 1 minute,
    responseCompression = false,
    responseCompressionLevel = 6,
    maxWsFramePayloadLength = 65536,
    wsLogger = ServerConfig.Defaults.logger,
    simpleWsLogging = false
  )
}
