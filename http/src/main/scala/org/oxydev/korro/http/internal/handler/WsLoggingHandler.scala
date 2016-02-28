/*
 * Copyright (C) 2015, 2016  Vladimir Konstantinov, Yuriy Gintsyak
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
package org.oxydev.korro.http.internal.handler

import org.oxydev.korro.util.log.Logger.Logger

import io.netty.handler.codec.http.websocketx._

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class WsLoggingHandler(logger: Logger) extends LoggingHandler(logger) {

  override protected def text(name: String): PartialFunction[Any, String] = {
    case m: TextWebSocketFrame   => formatName(name) + "TEXT   | " + m.text
    case m: BinaryWebSocketFrame => formatName(name) + "BINARY |"
    case m: CloseWebSocketFrame  => formatName(name) + "CLOSE  |"
    case m: PingWebSocketFrame   => formatName(name) + "PING   |"
    case m: PongWebSocketFrame   => formatName(name) + "PONG   |"
  }

  private def formatName(name: String): String = String.format("%-8s", name) + " : "
}
