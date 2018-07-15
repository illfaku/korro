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
package io.cafebabe.korro.util.protocol.jsonrpc

import org.json4s._

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
case class JsonRpcError(code: Int, message: String, id: Option[Int] = None) extends JsonRpcMessage {

  override val toJson = JObject(
    List(
      ("error", JObject(
        ("code", JInt(code)),
        ("message", JString(message))
      ))
    ) ++ id.map("id" -> JInt(_))
  )
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object JsonRpcError {

  object Codes {
    val ParseError = -32700
    val InvalidRequest = -32600
    val MethodNotFound = -32601
    val InvalidParams = -32602
    val InternalError = -32603
  }

  def parseError(message: String, id: Option[Int] = None): JsonRpcError = apply(Codes.ParseError, message, id)
  def invalidRequest(message: String, id: Option[Int] = None): JsonRpcError = apply(Codes.InvalidRequest, message, id)
  def methodNotFound(message: String, id: Option[Int] = None): JsonRpcError = apply(Codes.MethodNotFound, message, id)
  def invalidParams(message: String, id: Option[Int] = None): JsonRpcError = apply(Codes.InvalidParams, message, id)
  def internalError(message: String, id: Option[Int] = None): JsonRpcError = apply(Codes.InternalError, message, id)

  def from(json: JValue): Option[JsonRpcError] = json match {
    case JObject(fields) =>
      (for {
        ("error", JObject(error)) <- fields
        ("code", JInt(code)) <- error
        ("message", JString(message)) <- error
      } yield JsonRpcError(code.toInt, message, findId(fields))).headOption
    case _ => None
  }

  private def findId(fields: List[(String, JValue)]): Option[Int] = {
    (for (("id", JInt(id)) <- fields) yield id.toInt).headOption
  }
}
