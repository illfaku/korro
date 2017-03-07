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
import io.cafebabe.korro.internal.handler.{HttpMessageDecoder, HttpMessageEncoder, LoggingChannelHandler}
import io.cafebabe.korro.util.config.wrapped
import io.cafebabe.korro.util.log.Logger

import akka.actor.ActorRef
import com.typesafe.config.Config
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory

import java.net.URL

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class HttpChannelInitializer(config: Config, url: URL, req: HttpRequest, sender: ActorRef)
  extends ChannelInitializer[SocketChannel] {

  private val korroEncoder = new HttpMessageEncoder
  private val loggingHandler = new LoggingChannelHandler(Logger("korro-channel"))

  override def initChannel(ch: SocketChannel): Unit = {
    val pipeline = ch.pipeline

    Option(url.getProtocol).filter(_.equalsIgnoreCase("https")).map { _ =>
      SslContextBuilder.forClient.trustManager(InsecureTrustManagerFactory.INSTANCE).build
    } foreach { ctx =>
      pipeline.addLast("ssl", ctx.newHandler(ch.alloc()))
    }

    pipeline.addLast("http-codec", new HttpClientCodec)
    pipeline.addLast("logging", loggingHandler)
    pipeline.addLast("korro-encoder", korroEncoder)
    pipeline.addLast("korro-decoder", new HttpMessageDecoder(config.findBytes("maxContentLength").getOrElse(65536L)))
    pipeline.addLast("http", new HttpChannelHandler(url, req, sender))
  }
}
