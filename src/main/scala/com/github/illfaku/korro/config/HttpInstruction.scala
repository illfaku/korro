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
import scala.util.Try

sealed trait HttpInstruction

object HttpInstruction {

  case class RequestTimeout(timeout: FiniteDuration) extends HttpInstruction

  case class MaxContentLength(length: Int) extends HttpInstruction

  case class MaxWsFramePayloadLength(length: Int) extends HttpInstruction

  case class HttpLogger(name: String) extends HttpInstruction

  case class HttpContentLogging(format: BytesLoggingFormat.Value) extends HttpInstruction


  object BytesLoggingFormat extends Enumeration {

    val Off = Value("off")
    val Text = Value("text")
    val Base64 = Value("base64")

    def get(name: String): Option[Value] = Try(withName(name)).toOption
  }


  def extract(config: Config): List[HttpInstruction] = {
    List(
      config.findFiniteDuration("request-timeout").map(RequestTimeout),
      config.findInt("max-content-length").map(MaxContentLength),
      config.findInt("max-ws-frame-payload-length").map(MaxWsFramePayloadLength),
      config.findString("http-logger").map(HttpLogger),
      config.findString("http-content-logging").flatMap(BytesLoggingFormat.get).map(HttpContentLogging)
    ).flatten
  }
}
