/*
 * Copyright 2018 Vladimir Konstantinov
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
package com.github.illfaku.korro.dto.ws

import com.github.illfaku.korro.config.HttpInstruction
import com.github.illfaku.korro.dto.{HttpParams, HttpRequest}

import akka.actor.ActorRef

import java.net.URL

/**
 * Request for a WebSocket handshake.
 *
 * @param actor Actor that will process handshake response and WebSocket frames.
 * @param uri URI of handshake request.
 * @param headers HTTP headers of a handshake request.
 */
case class WsHandshakeRequest(
  actor: ActorRef,
  uri: HttpRequest.Uri = HttpRequest.Uri(""),
  headers: HttpParams = HttpParams.empty
) {

  /**
   * Creates [[com.github.illfaku.korro.dto.HttpRequest.Outgoing HttpRequest.Outgoing]] command for HTTP client.
   * Concatenates path from it with uri from this request.
   */
  def to(url: URL, instructions: List[HttpInstruction] = Nil): WsHandshakeRequest.Outgoing = {
    val req = copy(uri = uri.withPrefix(url.getPath))
    new WsHandshakeRequest.Outgoing(req, url, instructions)
  }
}

object WsHandshakeRequest {

  /**
   * Command for HTTP client created by `WsHandshakeRequest#to` methods.
   */
  class Outgoing private[korro] (val req: WsHandshakeRequest, val url: URL, val instructions: List[HttpInstruction])

  private[korro] object Outgoing {
    def unapply(out: Outgoing): Option[(WsHandshakeRequest, URL, List[HttpInstruction])] = {
      Some(out.req, out.url, out.instructions)
    }
  }
}
