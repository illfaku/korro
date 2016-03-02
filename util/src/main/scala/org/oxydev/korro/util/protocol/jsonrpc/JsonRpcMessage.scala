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
package org.oxydev.korro.util.protocol.jsonrpc

import org.json4s._

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
sealed trait JsonRpcMessage {
  def toJson: JValue
}

object JsonRpcMessage {

  implicit def toJson(msg: JsonRpcMessage): JValue = msg.toJson

  def from(json: JValue): Option[JsonRpcMessage] = {
    JsonRpcRequest.from(json) orElse JsonRpcResult.from(json) orElse JsonRpcError.from(json)
  }
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
case class JsonRpcRequest(method: String, version: String, params: JValue, id: Option[Int] = None) extends JsonRpcMessage {

  override lazy val toJson = JObject(
    List(
      ("method", JString(method)),
      ("version", JString(version)),
      ("params", params)
    ) ++ id.map("id" -> JInt(_))
  )
}

object JsonRpcRequest {

  def from(json: JValue): Option[JsonRpcRequest] = json match {
    case JObject(fields) =>
      (for {
        ("method", JString(method)) <- fields
        ("params", params) <- fields
      } yield JsonRpcRequest(method, findVersion(fields), params, findId(fields))).headOption
    case _ => None
  }

  private def findVersion(fields: List[(String, JValue)]): String = {
    (for (("version", JString(version)) <- fields) yield version).headOption.getOrElse("1.0")
  }

  private def findId(fields: List[(String, JValue)]): Option[Int] = {
    (for (("id", JInt(id)) <- fields) yield id.toInt).headOption
  }
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
sealed trait JsonRpcReply extends JsonRpcMessage {

  def withId(id: Int): JsonRpcReply = withId(Some(id))

  def withId(id: Option[Int]): JsonRpcReply = this match {
    case j: JsonRpcResult => j.copy(id = id)
    case j: JsonRpcError => j.copy(id = id)
  }
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
case class JsonRpcResult(result: JValue, id: Option[Int] = None) extends JsonRpcReply {

  override lazy val toJson = JObject(
    List(
      ("result", result)
    ) ++ id.map("id" -> JInt(_))
  )
}

object JsonRpcResult {

  def from(json: JValue): Option[JsonRpcResult] = json match {
    case JObject(fields) =>
      (for {
        ("result", result) <- fields
      } yield JsonRpcResult(result, findId(fields))).headOption
    case _ => None
  }

  private def findId(fields: List[(String, JValue)]): Option[Int] = {
    (for (("id", JInt(id)) <- fields) yield id.toInt).headOption
  }
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
case class JsonRpcError(code: Int, message: String, id: Option[Int] = None) extends JsonRpcReply {

  override lazy val toJson = JObject(
    List(
      ("error", JObject(
        ("code", JInt(code)),
        ("message", JString(message))
      ))
    ) ++ id.map("id" -> JInt(_))
  )
}

object JsonRpcError {

  object Codes {
    val ParseError = -32700
    val InvalidRequest = -32600
    val MethodNotFound = -32601
    val InvalidParams = -32602
    val InternalError = -32603
  }

  def ParseError(message: String, id: Option[Int] = None): JsonRpcError = apply(Codes.ParseError, message, id)
  def InvalidRequest(message: String, id: Option[Int] = None): JsonRpcError = apply(Codes.InvalidRequest, message, id)
  def MethodNotFound(message: String, id: Option[Int] = None): JsonRpcError = apply(Codes.MethodNotFound, message, id)
  def InvalidParams(message: String, id: Option[Int] = None): JsonRpcError = apply(Codes.InvalidParams, message, id)
  def InternalError(message: String, id: Option[Int] = None): JsonRpcError = apply(Codes.InternalError, message, id)

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
