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
case class JsonRpcNotification(method: String, params: JValue) extends JsonRpcMessage {
  override def toJson: JValue = JObject(
    ("method", JString(method)),
    ("params", params)
  )
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object JsonRpcNotification {
  def from(json: JValue): Option[JsonRpcNotification] = json match {
    case JObject(fields) =>
      (for {
        ("method", JString(method)) <- fields
        ("params", params) <- fields
      } yield JsonRpcNotification(method, params)).headOption
    case _ => None
  }
}
