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

import org.oxydev.korro.http.api.ws.WsConnection
import org.oxydev.korro.http.internal.common.ChannelFutureExt
import org.oxydev.korro.http.internal.common.handler._
import org.oxydev.korro.http.internal.server.config.WsConfig
import org.oxydev.korro.util.log.Logging

import akka.actor.ActorRef
import io.netty.channel._
import io.netty.handler.codec.http.websocketx._
import io.netty.handler.codec.http.{FullHttpRequest, HttpHeaderNames}

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
    val location = s"ws://${req.headers.get(HttpHeaderNames.HOST)}/${new URI(req.uri).getPath}"
    val factory = new WebSocketServerHandshakerFactory(location, null, true, config.maxFramePayloadLength)
    Option(factory.newHandshaker(req))
  }

  private def handshake(handshaker: WebSocketServerHandshaker, channel: Channel, req: FullHttpRequest): Unit = {
    handshaker.handshake(channel, req) foreach { future =>
      if (future.isSuccess) {
        val pipeline = channel.pipeline
        pipeline.remove("http")
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
    val host = req.headers.get(HttpHeaderNames.HOST)
    val path = {
      val pos = req.uri.indexOf('?')
      if (pos >= 0) req.uri.substring(0, pos) else req.uri
    }
    val sourceIp = config.sourceIpHeader.flatMap(name => Option(req.headers.get(name))) getOrElse {
      channel.remoteAddress.asInstanceOf[InetSocketAddress].getHostString
    }
    WsConnection(host, path, sourceIp)
  }
}
