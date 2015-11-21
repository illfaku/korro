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

import io.cafebabe.korro.api.route.WsRoute
import io.cafebabe.korro.netty.ChannelFutureExt
import io.cafebabe.korro.util.config.wrapped
import io.cafebabe.korro.util.log.Logging

import akka.actor.ActorContext
import com.typesafe.config.Config
import io.netty.channel._
import io.netty.handler.codec.http.websocketx.{WebSocketServerHandshaker, WebSocketServerHandshakerFactory}
import io.netty.handler.codec.http.{FullHttpRequest, HttpHeaders}

import java.net.{InetSocketAddress, URI}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class WsHandshakeChannelHandler(config: Config, route: WsRoute)(implicit context: ActorContext)
  extends SimpleChannelInboundHandler[FullHttpRequest] with Logging {

  private val maxFramePayloadLength = config.findBytes("WebSocket.maxFramePayloadLength").getOrElse(65536L).toInt
  private val compression = config.findBoolean("WebSocket.compression").getOrElse(false)

  override def channelRead0(ctx: ChannelHandlerContext, msg: FullHttpRequest): Unit = {
    newHandshaker(msg) match {
      case Some(handshaker) => handshake(handshaker, ctx.channel, msg)
      case None => WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel).addListener(ChannelFutureListener.CLOSE)
    }
  }

  private def newHandshaker(req: FullHttpRequest): Option[WebSocketServerHandshaker] = {
    val location = s"ws://${req.headers.get(HttpHeaders.Names.HOST)}/${new URI(req.getUri).getPath}"
    val factory = new WebSocketServerHandshakerFactory(location, null, true, maxFramePayloadLength)
    Option(factory.newHandshaker(req))
  }

  private def handshake(handshaker: WebSocketServerHandshaker, channel: Channel, req: FullHttpRequest): Unit = {
    val host = extractHost(channel, req)
    handshaker.handshake(channel, req) foreach { future =>
      if (future.isSuccess) {
        val pipeline = channel.pipeline
        pipeline.remove("http")
        pipeline.remove("korro-encoder")
        pipeline.remove("korro-decoder")
        pipeline.remove(this)
        if (compression) pipeline.addBefore("logging", "ws-compression", new WsCompressionChannelHandler)
        pipeline.addAfter("logging", "ws", new WsChannelHandler(host, route.actor))
      } else {
        log.error(future.cause, "Error during handshake.")
        channel.close()
      }
    }
  }

  private def extractHost(channel: Channel, req: FullHttpRequest): String = {
    val host = req.headers.get("X-Real-IP")
    if (host != null) host
    else channel.remoteAddress match {
      case address: InetSocketAddress => address.getHostString
      case _ => "UNKNOWN"
    }
  }
}
