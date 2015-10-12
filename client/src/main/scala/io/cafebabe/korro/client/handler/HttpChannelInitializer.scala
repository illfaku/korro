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
package io.cafebabe.korro.client.handler

import io.cafebabe.korro.api.http.HttpRequest

import akka.actor.ActorRef
import com.typesafe.config.Config
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.{HttpClientCodec, HttpObjectAggregator}
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory

import java.net.URI

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class HttpChannelInitializer(config: Config, uri: URI, req: HttpRequest, sender: ActorRef)
  extends ChannelInitializer[SocketChannel] {

  override def initChannel(ch: SocketChannel): Unit = {
    val pipe = ch.pipeline

    Option(uri.getScheme).filter(_.equalsIgnoreCase("https")).map { _ =>
      SslContextBuilder.forClient.trustManager(InsecureTrustManagerFactory.INSTANCE).build
    } foreach { ctx =>
      pipe.addLast("ssl", ctx.newHandler(ch.alloc()))
    }

    pipe.addLast("http-codec", new HttpClientCodec)
    pipe.addLast("http-aggregate", new HttpObjectAggregator(1048576))
    pipe.addLast("http", new HttpChannelHandler(uri, req, sender))
  }
}
