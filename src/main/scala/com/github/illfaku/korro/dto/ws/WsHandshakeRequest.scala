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

import com.github.illfaku.korro.dto.{HttpParams, HttpRequest}

import akka.actor.ActorRef

/**
 * Request for a WebSocket handshake.
 *
 * @param uri URI of a handshake request.
 * @param actor Actor that will process handshake response and WebSocket frames.
 * @param headers HTTP headers of a handshake request.
 */
case class WsHandshakeRequest(uri: HttpRequest.Uri, actor: ActorRef, headers: HttpParams = HttpParams.empty)
