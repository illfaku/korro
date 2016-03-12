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

import org.oxydev.korro.http.api.ws.WsConnection
import org.oxydev.korro.http.internal.common.ChannelFutureExt
import org.oxydev.korro.http.internal.common.handler._
import org.oxydev.korro.http.internal.server.config.WsConfig
import org.oxydev.korro.util.log.Logging

import akka.actor.ActorRef
import io.netty.channel._
import io.netty.handler.codec.http.websocketx._
import io.netty.handler.codec.http.{FullHttpRequest, HttpHeaders}

import java.net.{InetSocketAddress, URI}

/**
 * Completes WebSocket handshake with client and modifies channel pipeline to handle WebSocket frames.
 *
 * @param config Server WebSocket configuration.
 * @param parent Reference of associated HttpServerActor.
 * @param route Path to actor to which WsMessages will be sent.
 */
class WsHandshakeHandler(config: WsConfig, parent: ActorRef, route: String)
  extends SimpleChannelInboundHandler[FullHttpRequest] with Logging {

  override def channelRead0(ctx: ChannelHandlerContext, msg: FullHttpRequest): Unit = {
    newHandshaker(msg) match {
      case Some(handshaker) => handshake(handshaker, ctx.channel, msg)
      case None => WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel).closeChannel()
    }
  }

  private def newHandshaker(req: FullHttpRequest): Option[WebSocketServerHandshaker] = {
    val location = s"ws://${req.headers.get(HttpHeaders.Names.HOST)}/${new URI(req.getUri).getPath}"
    val factory = new WebSocketServerHandshakerFactory(location, null, true, config.maxFramePayloadLength)
    Option(factory.newHandshaker(req))
  }

  private def handshake(handshaker: WebSocketServerHandshaker, channel: Channel, req: FullHttpRequest): Unit = {
    handshaker.handshake(channel, req) foreach { future =>
      if (future.isSuccess) {
        val pipeline = channel.pipeline
        pipeline.remove("http")
        pipeline.remove("http-aggregator")
        pipeline.addBefore("logging", "ws-aggregator", new WebSocketFrameAggregator(config.maxFramePayloadLength))
        if (config.compression) pipeline.addBefore("logging", "ws-compression", new WsCompressionEncoder)
        if (config.decompression) pipeline.addBefore("logging", "ws-decompression", new WsCompressionDecoder)
        pipeline.addAfter("logging", "ws-logging", new WsLoggingHandler(config.logger))
        pipeline.addAfter("ws-logging", "ws-standard", WsStandardBehaviorHandler)
        pipeline.addAfter("ws-standard", "ws-codec", WsMessageCodec)
        pipeline.addAfter("ws-codec", "ws", new WsChannelHandler(parent, connection(channel, req), route))
        pipeline.remove(this)
      } else {
        log.error(future.cause, "Error during handshake.")
        channel.close()
      }
    }
  }

  private def connection(channel: Channel, req: FullHttpRequest): WsConnection = {
    val host = req.headers.get(HttpHeaders.Names.HOST)
    val path = {
      val pos = req.getUri.indexOf('?')
      if (pos >= 0) req.getUri.substring(0, pos) else req.getUri
    }
    val sourceIp = config.sourceIpHeader.flatMap(name => Option(req.headers.get(name))) getOrElse {
      channel.remoteAddress.asInstanceOf[InetSocketAddress].getHostString
    }
    WsConnection(host, path, sourceIp)
  }
}
