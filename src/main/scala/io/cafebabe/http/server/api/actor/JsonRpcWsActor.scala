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
package io.cafebabe.http.server.api.actor

import io.cafebabe.http.server.api.protocol.jsonrpc._
import io.cafebabe.http.server.api.ws.{ConnectWsMessage, DisconnectWsMessage, TextWsMessage}

import akka.actor.Actor
import org.json4s.ParserUtil.ParseException
import org.json4s._
import org.json4s.native.JsonParser.parse

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
trait JsonRpcWsActor extends Actor {

  type JsonRpcReceive = PartialFunction[JsonRpcMessage, Unit]

  override final def receive: Receive = {
    case ConnectWsMessage(host) =>
      tryReceive(JsonRpcNotification("connected", JObject("host" -> JString(host))), unhandled)
    case DisconnectWsMessage =>
      tryReceive(JsonRpcNotification("disconnected", JNothing), unhandled)
    case TextWsMessage(text) =>
      try {
        val json = parse(text)
        JsonRpcRequest.from(json) orElse JsonRpcNotification.from(json) match {
          case Some(msg) => tryReceive(msg, notFound)
          case None => sender ! JsonRpcError.parseError(s"Failed to parse as JSON-RPC: $text", -1)
        }
      } catch {
        case e: ParseException => sender ! JsonRpcError.parseError(s"Failed to parse as JSON: $text", -1)
      }
  }

  private def tryReceive(msg: JsonRpcMessage, default: (JsonRpcMessage) => Unit): Unit =
    receiveJsonRpc.applyOrElse(msg, default)

  private def notFound(msg: JsonRpcMessage): Unit = msg match {
    case JsonRpcRequest(method, _, id) => sender ! JsonRpcError.methodNotFound(s"Method $method is not found.", id)
    case JsonRpcNotification(method, _) => sender ! JsonRpcError.methodNotFound(s"Method $method is not found.", -1)
  }

  def receiveJsonRpc: JsonRpcReceive
}
