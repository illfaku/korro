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
