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
package io.cafebabe.korro.internal.ws

import io.cafebabe.korro.api.ws.{WsProtocol, TextWsMessage, WsMessage}
import io.cafebabe.korro.util.protocol.jsonrpc.JsonRpcMessage

import org.json4s.native.JsonParser.parseOpt
import org.json4s.native.JsonMethods.{render, compact}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object JsonRpcWsProtocol extends WsProtocol {

  override def decode(msg: WsMessage): Option[Any] = msg match {
    case TextWsMessage(text) => parseOpt(text).flatMap(JsonRpcMessage.from) orElse Some(msg)
    case _ => Some(msg)
  }

  override def encode(msg: Any): Option[WsMessage] = msg match {
    case jr: JsonRpcMessage => Some(TextWsMessage(compact(render(jr))))
    case m: WsMessage => Some(m)
    case _ => None
  }
}
