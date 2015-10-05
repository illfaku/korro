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
package io.cafebabe.korro.server.handler

import io.cafebabe.korro.util.config.wrapped

import akka.actor.{ActorContext, ActorSystem}
import com.typesafe.config.Config
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http._

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class HttpChannelInitializer(config: Config)(implicit context: ActorContext) extends ChannelInitializer[SocketChannel] {

  private val maxContentLength = config.findBytes("HTTP.maxContentLength").getOrElse(65536L).toInt

  private val compressionLevel = config.findInt("HTTP.compression")

  override def initChannel(ch: SocketChannel): Unit = {
    val pipeline = ch.pipeline

    pipeline.addLast(new HttpRequestDecoder)
    pipeline.addLast(new HttpObjectAggregator(maxContentLength))
    pipeline.addLast(new HttpContentDecompressor)

    pipeline.addLast(new HttpResponseEncoder)
    compressionLevel foreach { level => pipeline.addLast(new HttpContentCompressor(level)) }

    pipeline.addLast(new HttpChannelHandler(config.getInt("port")))
  }
}
