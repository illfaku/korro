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
package com.github.illfaku.korro.internal.common

import com.github.illfaku.korro.config.HttpInstruction
import com.github.illfaku.korro.config.HttpInstruction._

import scala.concurrent.duration._

private[internal] case class HttpInstructions(
  requestTimeout: FiniteDuration = 5 seconds,
  maxContentLength: Int = Int.MaxValue,
  maxWsFramePayloadLength: Int = Int.MaxValue,
  httpLogger: String = "korro-http",
  httpContentLogging: BytesLoggingFormat.Value = BytesLoggingFormat.Text
) {

  def merge(instructions: List[HttpInstruction]): HttpInstructions = instructions match {
    case RequestTimeout(timeout) :: tail => copy(requestTimeout = timeout).merge(tail)
    case MaxContentLength(length) :: tail => copy(maxContentLength = length).merge(tail)
    case MaxWsFramePayloadLength(length) :: tail => copy(maxWsFramePayloadLength = length).merge(tail)
    case HttpLogger(name) :: tail => copy(httpLogger = name).merge(tail)
    case HttpContentLogging(format) :: tail => copy(httpContentLogging = format).merge(tail)
    case Nil => this
  }
}
