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
 * Modified representation of JSON-RPC protocol message.
 *
 * @see http://www.jsonrpc.org/specification
 */
sealed trait JsonRpcMessage {

  /**
   * Converts this message to JSON.
   */
  def toJson: JValue

  /**
   * Adds id to this message.
   */
  def withId(id: Int): JsonRpcMessage = withId(Some(id))

  /**
   * Adds optional id to this message.
   */
  def withId(id: Option[Int]): JsonRpcMessage = this match {
    case j: JsonRpcRequest => j.copy(id = id)
    case j: JsonRpcResult => j.copy(id = id)
    case j: JsonRpcError => j.copy(id = id)
  }
}

object JsonRpcMessage {

  /**
   * Implicit conversion of JsonRpcMessages to JSON.
   */
  implicit def toJson(msg: JsonRpcMessage): JValue = msg.toJson

  /**
   * Tries to convert JSON to one of JsonRpcMessages.
   */
  def from(json: JValue): Option[JsonRpcMessage] = {
    JsonRpcRequest.from(json) orElse JsonRpcResult.from(json) orElse JsonRpcError.from(json)
  }
}

/**
 * Modified representation of JSON-RPC protocol request or notification (if id is missing).
 *
 * @see http://www.jsonrpc.org/specification#request_object
 * @param method Name of the method to be invoked.
 * @param version Version of the method (not present in specification).
 * @param params Parameters needed for this request processing. Can be any JSON value (object, array or even nothing).
 * @param id Optional request identifier.
 */
case class JsonRpcRequest(method: String, version: String, params: JValue, id: Option[Int] = None) extends JsonRpcMessage {

  /**
   * This request as JSON.
   */
  override lazy val toJson = JObject(
    List(
      ("method", JString(method)),
      ("version", JString(version)),
      ("params", params)
    ) ++ id.map("id" -> JInt(_))
  )
}

object JsonRpcRequest {

  /**
   * Tries to convert JSON to JsonRpcRequest.
   */
  def from(json: JValue): Option[JsonRpcRequest] = json match {
    case JObject(fields) =>
      (for {
        ("method", JString(method)) <- fields
      } yield JsonRpcRequest(method, findVersion(fields), findParams(fields), findId(fields))).headOption
    case _ => None
  }

  private def findVersion(fields: List[(String, JValue)]): String = {
    (for (("version", JString(version)) <- fields) yield version).headOption.getOrElse("1.0")
  }

  private def findParams(fields: List[(String, JValue)]): JValue = {
    (for (("params", params) <- fields) yield params).headOption.getOrElse(JNothing)
  }

  private def findId(fields: List[(String, JValue)]): Option[Int] = {
    (for (("id", JInt(id)) <- fields) yield id.toInt).headOption
  }
}

/**
 * Modified representation of JSON-RPC protocol response divided into two separate entities.
 *
 * @see http://www.jsonrpc.org/specification#response_object
 */
sealed trait JsonRpcResponse extends JsonRpcMessage

/**
 * Successful JsonRpcResponse.
 *
 * @param result Some JSON representing result.
 * @param id Optional response identifier.
 */
case class JsonRpcResult(result: JValue, id: Option[Int] = None) extends JsonRpcResponse {

  /**
   * This response as JSON.
   */
  override lazy val toJson = JObject(
    List(
      ("result", result)
    ) ++ id.map("id" -> JInt(_))
  )
}

object JsonRpcResult {

  /**
   * Tries to convert JSON to JsonRpcResult.
   */
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
 * Unsuccessful JsonRpcResponse.
 *
 * @param code Error code.
 * @param message Error message.
 * @param id Optional response identifier.
 */
case class JsonRpcError(code: Int, message: String, id: Option[Int] = None) extends JsonRpcResponse {

  /**
   * This response as JSON.
   */
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

  /**
   * Pre-defined error codes.
   */
  object Codes {
    val ParseError = -32700
    val InvalidRequest = -32600
    val MethodNotFound = -32601
    val InvalidParams = -32602
    val InternalError = -32603
  }

  /**
   * Returns JsonRpcError with pre-defined `Parse error` code.
   */
  def ParseError(message: String, id: Option[Int] = None): JsonRpcError = apply(Codes.ParseError, message, id)

  /**
   * Returns JsonRpcError with pre-defined `Invalid Request` code.
   */
  def InvalidRequest(message: String, id: Option[Int] = None): JsonRpcError = apply(Codes.InvalidRequest, message, id)

  /**
   * Returns JsonRpcError with pre-defined `Method not found` code.
   */
  def MethodNotFound(message: String, id: Option[Int] = None): JsonRpcError = apply(Codes.MethodNotFound, message, id)

  /**
   * Returns JsonRpcError with pre-defined `Invalid params` code.
   */
  def InvalidParams(message: String, id: Option[Int] = None): JsonRpcError = apply(Codes.InvalidParams, message, id)

  /**
   * Returns JsonRpcError with pre-defined `Internal error` code.
   */
  def InternalError(message: String, id: Option[Int] = None): JsonRpcError = apply(Codes.InternalError, message, id)

  /**
   * Tries to convert JSON to JsonRpcError.
   */
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
