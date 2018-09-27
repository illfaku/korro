/*
 * Copyright (C) 2015  Vladimir Konstantinov, Yuriy Gintsyak
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.cafebabe.korro.api.ws

sealed trait WsMessage

case class ConnectWsMessage(uri: String, host: String) extends WsMessage
case class DisconnectWsMessage(code: Int = 1001, reason: Option[String] = None) extends WsMessage
case object PingWsMessage extends WsMessage
case object PongWsMessage extends WsMessage
case class TextWsMessage(text: String) extends WsMessage
case class BinaryWsMessage(bytes: Array[Byte]) extends WsMessage
