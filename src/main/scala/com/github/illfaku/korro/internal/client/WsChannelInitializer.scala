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
package com.github.illfaku.korro.internal.client

import com.github.illfaku.korro.config.ClientConfig
import com.github.illfaku.korro.dto.HttpParams
import com.github.illfaku.korro.internal.common.{HttpInstructions, HttpLoggingHandler, WsMessageCodec}

import akka.actor.ActorRef
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.websocketx.{WebSocketClientProtocolHandler, WebSocketFrameAggregator, WebSocketVersion}
import io.netty.handler.codec.http.{DefaultHttpHeaders, HttpClientCodec, HttpObjectAggregator}
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory

import java.net.URI

private[client] class WsChannelInitializer(
  parent: ActorRef,
  config: ClientConfig,
  uri: URI,
  headers: HttpParams,
  inActor: ActorRef
) extends ChannelInitializer[SocketChannel] {

  private val instructions = HttpInstructions().merge(config.instructions)

  private val customHeaders = {
    val nettyHeaders = new DefaultHttpHeaders()
    headers.entries.foreach(h => nettyHeaders.add(h._1, h._2))
    nettyHeaders
  }

  override def initChannel(ch: SocketChannel): Unit = {

    if (config.url.exists(isSsl)) {
      val sslCtx = SslContextBuilder.forClient.trustManager(InsecureTrustManagerFactory.INSTANCE).build
      ch.pipeline.addLast("ssl", sslCtx.newHandler(ch.alloc()))
    }

    ch.pipeline.addLast("netty-http-codec", new HttpClientCodec)
    ch.pipeline.addLast("netty-logger", new LoggingHandler(config.nettyLogger, LogLevel.TRACE))
    ch.pipeline.addLast("korro-logger", new HttpLoggingHandler(instructions))
    ch.pipeline.addLast("netty-http-aggregator", new HttpObjectAggregator(instructions.maxContentLength))
    ch.pipeline.addLast("netty-ws-handler", new WebSocketClientProtocolHandler(
      uri, WebSocketVersion.V13, null, true, customHeaders, instructions.maxWsFramePayloadLength, false
    ))
    ch.pipeline.addLast("netty-ws-aggregator", new WebSocketFrameAggregator(instructions.maxContentLength))
    ch.pipeline.addLast("korro-ws-codec", WsMessageCodec)
    ch.pipeline.addLast("korro-handler", new WsChannelHandler(parent, inActor))
  }
}
