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
case class JsonRpcRequest(method: String, version: String, params: JValue, id: Option[Int] = None) extends JsonRpcMessage {

  override val toJson = JObject(
    List(
      ("method", JString(method)),
      ("version", JString(version)),
      ("params", params)
    ) ++ id.map("id" -> JInt(_))
  )
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
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
