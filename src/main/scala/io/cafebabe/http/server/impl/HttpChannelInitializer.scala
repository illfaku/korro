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
package io.cafebabe.http.server.impl

import io.cafebabe.util.config.wrapped

import akka.actor.ActorSystem
import com.typesafe.config.Config
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpRequestDecoder, HttpResponseEncoder}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class HttpChannelInitializer(config: Config, actors: ActorSystem) extends ChannelInitializer[SocketChannel] {

  private val routes = new Routes(config)

  private val maxContentLength = config.findBytes("HTTP.maxContentLength").getOrElse(65536L).toInt

  override def initChannel(ch: SocketChannel): Unit = {
    val pipeline = ch.pipeline
    pipeline.addLast(new HttpRequestDecoder)
    pipeline.addLast(new HttpObjectAggregator(maxContentLength))
    pipeline.addLast(new HttpResponseEncoder)
    pipeline.addLast(new HttpChannelHandler(actors, routes))
  }
}
