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

import scala.concurrent.duration.FiniteDuration

sealed trait HttpInstruction

object HttpInstruction {

  case class RequestTimeout(timeout: FiniteDuration) extends HttpInstruction

  case class MaxContentLength(length: Long) extends HttpInstruction

//  case class ContentAsFile(enabled: Boolean) extends HttpInstruction

//  case class FileContentRemoveDelay(delay: Duration) extends HttpInstruction

//  case class Compression(enabled: Boolean) extends HttpInstruction

//  case class CompressionLevel(level: Int) extends HttpInstruction

  case class MaxWsFramePayloadLength(length: Int) extends HttpInstruction

  case class WsLogger(name: String) extends HttpInstruction

  case class SimpleWsLogging(enabled: Boolean) extends HttpInstruction


  def extract(config: Config): List[HttpInstruction] = {
    List(
      config.findFiniteDuration("request-timeout").map(HttpInstruction.RequestTimeout),
      config.findBytes("max-content-length").map(HttpInstruction.MaxContentLength),
//      config.findBoolean("content-as-file").map(HttpInstruction.ContentAsFile),
//      config.findDuration("file-content-remove-delay").map(HttpInstruction.FileContentRemoveDelay),
//      config.findBoolean("compression").map(HttpInstruction.Compression),
//      config.findInt("compression-level").map(HttpInstruction.CompressionLevel),
      config.findInt("max-ws-frame-payload-length").map(HttpInstruction.MaxWsFramePayloadLength),
      config.findString("ws-logger").map(HttpInstruction.WsLogger),
      config.findBoolean("simple-ws-logging").map(HttpInstruction.SimpleWsLogging)
    ).flatten
  }
}
