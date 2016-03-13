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
package org.oxydev.korro.http.api.ws

/**
 * WebSocket data frame representations.
 *
 * @see https://tools.ietf.org/html/rfc6455#section-5.6
 */
sealed trait WsMessage

/**
 * WebSocket text frame representation.
 *
 * @param text Text data.
 */
case class TextWsMessage(text: String) extends WsMessage

/**
 * WebSocket binary frame representation.
 *
 * @param bytes Binary data.
 */
case class BinaryWsMessage(bytes: Array[Byte]) extends WsMessage
