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
package com.github.illfaku.korro.internal.server

import com.github.illfaku.korro.config.ServerConfig
import com.github.illfaku.korro.internal.common.{HttpInstructions, HttpLoggingHandler}

import akka.actor.ActorRef
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.logging.{LogLevel, LoggingHandler}

private[server] class HttpChannelInitializer(
  parent: ActorRef,
  config: ServerConfig,
  instructions: HttpInstructions
) extends ChannelInitializer[SocketChannel] {

  override def initChannel(ch: SocketChannel): Unit = {
    ch.pipeline.addLast("netty-http-codec", new HttpServerCodec)
    ch.pipeline.addLast("netty-logger", new LoggingHandler(config.nettyLogger, LogLevel.TRACE))
    ch.pipeline.addLast("korro-http-handler", new HttpChannelHandler(parent, instructions))
  }
}
