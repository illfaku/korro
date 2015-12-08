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

import io.cafebabe.korro.internal.ChannelFutureExt
import io.cafebabe.korro.internal.handler.{WsCompressionHandler, WsMessageCodec}
import io.cafebabe.korro.internal.ws.WsProtocolHandler
import io.cafebabe.korro.server.config.WsConfig
import io.cafebabe.korro.util.log.Logging

import akka.actor.ActorContext
import io.netty.channel._
import io.netty.handler.codec.http.websocketx.{WebSocketServerHandshaker, WebSocketServerHandshakerFactory}
import io.netty.handler.codec.http.{HttpHeaders, HttpRequest}

import java.net.{InetSocketAddress, URI}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class WsHandshakeHandler(config: WsConfig, route: String)(implicit context: ActorContext)
  extends SimpleChannelInboundHandler[HttpRequest] with Logging {

  override def channelRead0(ctx: ChannelHandlerContext, msg: HttpRequest): Unit = {
    newHandshaker(msg) match {
      case Some(handshaker) => handshake(handshaker, ctx.channel, msg)
      case None => WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel).closeChannel()
    }
  }

  private def newHandshaker(req: HttpRequest): Option[WebSocketServerHandshaker] = {
    val location = s"ws://${req.headers.get(HttpHeaders.Names.HOST)}/${new URI(req.getUri).getPath}"
    val factory = new WebSocketServerHandshakerFactory(location, null, true, config.maxFramePayloadLength)
    Option(factory.newHandshaker(req))
  }

  private def handshake(handshaker: WebSocketServerHandshaker, channel: Channel, req: HttpRequest): Unit = {
    handshaker.handshake(channel, req) foreach { future =>
      if (future.isSuccess) {
        val pipeline = channel.pipeline
        pipeline.remove("http")
        pipeline.remove(this)
        if (config.compression) pipeline.addBefore("logging", "ws-compression", new WsCompressionHandler)
        pipeline.addAfter("logging", "ws-codec", new WsMessageCodec)
        config.protocol.map(new WsProtocolHandler(_)).foreach(pipeline.addAfter("ws-codec", "protocol", _))
        pipeline.addBefore("last", "ws", new WsChannelHandler(extractHost(channel, req), route))
      } else {
        log.error(future.cause, "Error during handshake.")
        channel.close()
      }
    }
  }

  private def extractHost(channel: Channel, req: HttpRequest): String = {
    val host = req.headers.get("X-Real-IP")
    if (host != null) host
    else channel.remoteAddress match {
      case address: InetSocketAddress => address.getHostString
      case _ => "UNKNOWN"
    }
  }
}
