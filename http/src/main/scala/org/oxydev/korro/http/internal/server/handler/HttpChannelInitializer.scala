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
package org.oxydev.korro.http.internal.server.handler

import org.oxydev.korro.http.internal.server.config.ServerConfig

import akka.actor.{ActorRef, ActorContext}
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.{HttpContentCompressor, HttpServerCodec}
import io.netty.handler.logging.{LogLevel, LoggingHandler}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class HttpChannelInitializer(config: ServerConfig, parent: ActorRef) extends ChannelInitializer[SocketChannel] {

  private val httpHandler = new HttpChannelHandler(config, parent)
  private val loggingHandler = new LoggingHandler(config.http.logger, LogLevel.TRACE)
  private val lastHandler = new LastChannelHandler

  override def initChannel(ch: SocketChannel): Unit = {
    val pipeline = ch.pipeline
    pipeline.addLast("netty-codec", new HttpServerCodec)
    config.http.compressionLevel.map(new HttpContentCompressor(_)).foreach(pipeline.addLast("http-compressor", _))
    pipeline.addLast("logging", loggingHandler)
    pipeline.addLast("http", httpHandler)
    pipeline.addLast("last", lastHandler)
  }
}
