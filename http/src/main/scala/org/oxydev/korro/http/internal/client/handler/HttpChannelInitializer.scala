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
package org.oxydev.korro.http.internal.client.handler

import org.oxydev.korro.http.api.{HttpRequest, HttpResponse}
import org.oxydev.korro.http.internal.client.config.ClientConfig
import org.oxydev.korro.http.internal.common.handler.HttpMessageCodec

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory

import java.net.URL

import scala.concurrent.Promise

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class HttpChannelInitializer(config: ClientConfig, url: URL, req: HttpRequest, promise: Promise[HttpResponse])
  extends ChannelInitializer[SocketChannel] {

  override def initChannel(ch: SocketChannel): Unit = {

    if (url.getProtocol equalsIgnoreCase "https") {
      val sslCtx = SslContextBuilder.forClient.trustManager(InsecureTrustManagerFactory.INSTANCE).build
      ch.pipeline.addLast("ssl", sslCtx.newHandler(ch.alloc()))
    }

    ch.pipeline.addLast("http-codec", new HttpClientCodec)
    ch.pipeline.addLast("logging", new LoggingHandler(config.logger, LogLevel.TRACE))
    ch.pipeline.addLast("korro-codec", new HttpMessageCodec(65536L))
    ch.pipeline.addLast("http", new HttpChannelHandler(req, promise, config.requestTimeout))
  }
}
