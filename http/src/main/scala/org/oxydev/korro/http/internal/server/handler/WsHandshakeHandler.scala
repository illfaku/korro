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

import org.oxydev.korro.http.api.HttpParams
import org.oxydev.korro.http.api.ws.WsConnection
import org.oxydev.korro.http.internal.common.ChannelFutureExt
import org.oxydev.korro.http.internal.common.handler._
import org.oxydev.korro.http.internal.server.route.RouteInfo
import org.oxydev.korro.util.log.Logging

import io.netty.channel._
import io.netty.handler.codec.http.websocketx._
import io.netty.handler.codec.http.{FullHttpRequest, HttpHeaderNames}

import java.net.URI

import scala.collection.JavaConverters._

/**
 * Completes WebSocket handshake with client and modifies channel pipeline to handle WebSocket frames.
 *
 * @param route Path to actor to which WsMessages will be sent.
 */
class WsHandshakeHandler(route: RouteInfo)
  extends SimpleChannelInboundHandler[FullHttpRequest] with Logging {

  override def channelRead0(ctx: ChannelHandlerContext, msg: FullHttpRequest): Unit = {
    newHandshaker(msg) match {
      case Some(handshaker) => handshake(handshaker, ctx.channel, msg)
      case None => WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel).closeChannel()
    }
  }

  private def newHandshaker(req: FullHttpRequest): Option[WebSocketServerHandshaker] = {
    val location = s"ws://${req.headers.get(HttpHeaderNames.HOST)}/${new URI(req.uri).getPath}"
    val factory = new WebSocketServerHandshakerFactory(location, null, true, route.instructions.maxWsFramePayloadLength)
    Option(factory.newHandshaker(req))
  }

  private def handshake(handshaker: WebSocketServerHandshaker, channel: Channel, req: FullHttpRequest): Unit = {
    handshaker.handshake(channel, req) foreach { future =>
      if (future.isSuccess) {
        val pipeline = channel.pipeline
        pipeline.remove("http")
        pipeline.addBefore("logging", "ws-aggregator", new WebSocketFrameAggregator(route.instructions.maxWsFramePayloadLength))
        pipeline.addAfter("logging", "ws-logging", new WsLoggingHandler(route.instructions.wsLogger))
        pipeline.addAfter("ws-logging", "ws-standard", WsStandardBehaviorHandler)
        pipeline.addAfter("ws-standard", "ws-codec", WsMessageCodec)
        //val parent = ctx.channel.attr(Keys.wsParent).get
        //pipeline.addAfter("ws-codec", "ws", new WsChannelHandler(parent, connection(channel, req), route))
        pipeline.remove(this)
      } else {
        log.error(future.cause, "Error during handshake.")
        channel.close()
      }
    }
  }

  private def connection(channel: Channel, req: FullHttpRequest): WsConnection = {
    val headers = req.headers.asScala.map(header => header.getKey -> header.getValue)
    WsConnection(req.uri, HttpParams(headers.toSeq: _*))
  }
}
