/*
 * Copyright (C) 2015, 2016  Vladimir Konstantinov, Yuriy Gintsyak
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
