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
import io.cafebabe.korro.util.log.Logger

import akka.actor.ActorContext
import com.typesafe.config.Config
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel._
import io.netty.handler.codec.http.websocketx.{WebSocketServerHandshaker, WebSocketServerHandshakerFactory}
import io.netty.handler.codec.http.{FullHttpRequest, HttpHeaders}

import java.net.{InetSocketAddress, URI}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
@Sharable
class WsHandshakeChannelHandler(config: Config)(implicit context: ActorContext) extends ChannelInboundHandlerAdapter {

  private val log = Logger(getClass)

  private val maxFramePayloadLength = config.findBytes("WebSocket.maxFramePayloadLength").getOrElse(65536L).toInt
  private val compression = config.findBoolean("WebSocket.compression").getOrElse(false)

  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = msg match {
    case m: RoutedWsHandshake =>
      newHandshaker(m.req) match {
        case Some(handshaker) => handshake(handshaker, ctx, m)
        case None => WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel)
      }
      m.req.release()
    case _ => ctx.fireChannelRead(msg)
  }

  private def newHandshaker(req: FullHttpRequest): Option[WebSocketServerHandshaker] = {
    val location = s"ws://${req.headers.get(HttpHeaders.Names.HOST)}/${new URI(req.getUri).getPath}"
    val factory = new WebSocketServerHandshakerFactory(location, null, true, maxFramePayloadLength)
    Option(factory.newHandshaker(req))
  }

  private def handshake(handshaker: WebSocketServerHandshaker, ctx: ChannelHandlerContext, msg: RoutedWsHandshake): Unit = {
    val host = extractHost(ctx.channel, msg.req)
    handshaker.handshake(ctx.channel, msg.req) foreach { future =>
      if (future.isSuccess) {
        val pipeline = ctx.channel.pipeline
        pipeline.remove("http-decompressor")
        pipeline.remove("http-response")
        pipeline.remove("http")
        pipeline.remove("http-request")
        pipeline.remove("ws-handshake")
        if (compression) pipeline.addBefore("logging", "ws-compression", new WsCompressionChannelHandler)
        pipeline.addAfter("logging", "ws", new WsChannelHandler(host, msg.route.actor))
      } else {
        log.error("Error during handshake.", future.cause)
        future.channel.close()
      }
    }
  }

  private def extractHost(ch: Channel, req: FullHttpRequest): String = {
    val host = req.headers().get("X-Real-IP")
    if (host != null && host.trim.nonEmpty) host
    else ch.remoteAddress match {
      case address: InetSocketAddress => address.getHostString
      case _ => "UNKNOWN"
    }
  }
}

case class RoutedWsHandshake(req: FullHttpRequest, route: WsRoute)
