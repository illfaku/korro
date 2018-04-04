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
import com.github.illfaku.korro.dto.HttpRequest
import com.github.illfaku.korro.internal.common.{HttpInstructions, HttpLoggingHandler}

import akka.actor.ActorRef
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.{HttpClientCodec, HttpObjectAggregator}
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory

private[client] class HttpChannelInitializer(config: ClientConfig, req: HttpRequest, originator: ActorRef)
  extends ChannelInitializer[SocketChannel] {

  private val instructions = HttpInstructions().merge(config.instructions)

  override def initChannel(ch: SocketChannel): Unit = {

    if (config.url.exists(isSsl)) {
      val sslCtx = SslContextBuilder.forClient.trustManager(InsecureTrustManagerFactory.INSTANCE).build
      ch.pipeline.addLast("ssl", sslCtx.newHandler(ch.alloc()))
    }

    ch.pipeline.addLast("netty-http-codec", new HttpClientCodec)
    ch.pipeline.addLast("netty-logger", new LoggingHandler(config.nettyLogger, LogLevel.TRACE))
    ch.pipeline.addLast("korro-logger-init", new HttpLoggingInitializer(instructions))
    ch.pipeline.addLast("netty-http-aggregator", new HttpObjectAggregator(instructions.maxContentLength))
    ch.pipeline.addLast("korro-response-decoder", HttpResponseDecoder)
    ch.pipeline.addLast("korro-request-encoder", HttpRequestEncoder)
    ch.pipeline.addLast("korro-handler", new HttpChannelHandler(req, originator, instructions))
  }
}
