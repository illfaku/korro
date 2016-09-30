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
package org.oxydev.korro.http.api.route

import scala.concurrent.duration.{Duration, FiniteDuration}

sealed trait RouteInstruction

object RouteInstruction {

  case class RequestTimeout(duration: FiniteDuration) extends RouteInstruction

  case class MaxContentLength(length: Long) extends RouteInstruction

  case class ContentAsFile(value: Boolean) extends RouteInstruction

  case class FileContentRemoveDelay(delay: Duration) extends RouteInstruction

  case class ResponseCompressionLevel(level: Int) extends RouteInstruction

  case class WsLogger(name: String) extends RouteInstruction

  case class MaxWsFramePayloadLength(length: Int) extends RouteInstruction
}
