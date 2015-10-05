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

import io.cafebabe.korro.api.http.route.WsRoute
import io.cafebabe.korro.api.ws.ConnectWsMessage
import io.cafebabe.korro.server.actor.WsMessageSender
import io.cafebabe.korro.server.util.ChannelFutureExt

import akka.actor.ActorContext
import io.netty.channel._
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory._
import io.netty.handler.codec.http.{FullHttpRequest, HttpHeaders}
import org.slf4j.LoggerFactory

import java.net.{InetSocketAddress, URI}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class WsHandshakeChannelHandler(implicit context: ActorContext) extends ChannelInboundHandlerAdapter {

  private val log = LoggerFactory.getLogger(getClass)

  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = msg match {
    case RoutedWsHandshake(req, route) =>
      val location = s"ws://${req.headers.get(HttpHeaders.Names.HOST)}/${new URI(req.getUri).getPath}"
      val handshakerFactory = new WebSocketServerHandshakerFactory(location, null, true, route.maxFramePayloadLength)
      val handshaker = handshakerFactory.newHandshaker(req)
      if (handshaker != null) {
        val receiver = context.actorSelection(route.actor)
        val sender = WsMessageSender.create(ctx)
        val host = extractHost(ctx.channel, req)

        val pipeline = ctx.channel.pipeline
        if (route.compression) pipeline.addLast(new WsCompressionChannelHandler)
        pipeline.addLast(new WsChannelHandler(host, receiver, sender))

        handshaker.handshake(ctx.channel, req) foreach { future =>
          req.release()
          if (future.isSuccess) receiver.tell(new ConnectWsMessage(host), sender)
          else {
            log.error("Error during handshake.", future.cause)
            future.channel.close()
          }
        }
      } else {
        req.release()
        sendUnsupportedVersionResponse(ctx.channel).addListener(ChannelFutureListener.CLOSE)
      }
    case _ => ctx.fireChannelRead(msg)
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
