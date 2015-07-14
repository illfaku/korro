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
package io.cafebabe.http.server.api.protocol.jsonrpc

import org.json4s._

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
case class JsonRpcError(code: Int, message: String, id: Int) extends JsonRpcMessage {

  override val toJson = JObject(
    ("error", JObject(
      ("code", JInt(code)),
      ("message", JString(message))
    )),
    ("id", JInt(id))
  )
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object JsonRpcError {

  def parseError(message: String, id: Int): JsonRpcError = apply(-32700, message, id)
  def invalidRequest(message: String, id: Int): JsonRpcError = apply(-32600, message, id)
  def methodNotFound(message: String, id: Int): JsonRpcError = apply(-32601, message, id)
  def invalidParams(message: String, id: Int): JsonRpcError = apply(-32602, message, id)
  def internalError(message: String, id: Int): JsonRpcError = apply(-32603, message, id)

  def from(json: JValue): Option[JsonRpcError] = json match {
    case JObject(fields) =>
      (for {
        ("id", JInt(id)) <- fields
        ("error", JObject(error)) <- fields
        ("code", JInt(code)) <- error
        ("message", JString(message)) <- error
      } yield JsonRpcError(code.toInt, message, id.toInt)).headOption
    case _ => None
  }
}
