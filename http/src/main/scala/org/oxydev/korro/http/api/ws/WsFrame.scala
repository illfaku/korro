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
 * Marker trait for WebSocket frame representations.
 *
 * @see https://tools.ietf.org/html/rfc6455#section-5
 */
sealed trait WsFrame

/**
 * Marker trait for WebSocket control frame representations.
 *
 * @see https://tools.ietf.org/html/rfc6455#section-5.5
 */
sealed trait ControlWsFrame extends WsFrame

/**
 * WebSocket close frame representations.
 *
 * @see https://tools.ietf.org/html/rfc6455#section-5.5.1
 */
case class CloseWsFrame(status: Int, reason: String) extends ControlWsFrame

/**
 * WebSocket ping frame representations.
 *
 * @see https://tools.ietf.org/html/rfc6455#section-5.5.2
 */
case class PingWsFrame(bytes: Array[Byte]) extends ControlWsFrame

/**
 * WebSocket pong frame representations.
 *
 * @see https://tools.ietf.org/html/rfc6455#section-5.5.3
 */
case class PongWsFrame(bytes: Array[Byte]) extends ControlWsFrame

/**
 * Marker trait for WebSocket data frame representations.
 *
 * @see https://tools.ietf.org/html/rfc6455#section-5.6
 */
sealed trait DataWsFrame extends WsFrame

/**
 * WebSocket text frame representation.
 *
 * @see https://tools.ietf.org/html/rfc6455#section-5.6
 */
case class TextWsFrame(text: String) extends DataWsFrame

/**
 * WebSocket binary frame representation.
 *
 * @see https://tools.ietf.org/html/rfc6455#section-5.6
 */
case class BinaryWsFrame(bytes: Array[Byte]) extends DataWsFrame
