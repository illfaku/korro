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

import org.oxydev.korro.http.internal.common.handler.HttpMessageCodec
import org.oxydev.korro.http.internal.server.config.ServerConfig

import akka.actor.ActorRef
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
@Sharable
class HttpChannelHandler(config: ServerConfig, parent: ActorRef) extends SimpleChannelInboundHandler[HttpRequest] {

  private val NotFound = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND)

  override def channelRead0(ctx: ChannelHandlerContext, msg: HttpRequest): Unit = {
    if (isHandshake(msg)) doHandshake(ctx, msg) else doRequest(ctx, msg)
  }

  private def isHandshake(msg: HttpRequest): Boolean = {
    msg.headers.contains(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.UPGRADE, true) &&
    msg.headers.contains(HttpHeaders.Names.UPGRADE, HttpHeaders.Values.WEBSOCKET, true)
  }

  private def doRequest(ctx: ChannelHandlerContext, msg: HttpRequest): Unit = config.http.routes(msg) match {
    case Some(route) =>
      if (ctx.pipeline.get("http-codec") == null) {
        ctx.pipeline.addAfter(ctx.name, "http-codec", new HttpMessageCodec(config.http.maxContentLength))
      }
      if (ctx.pipeline.get("http-request") != null) {
        ctx.pipeline.remove("http-request")
      }
      ctx.pipeline.addAfter("http-codec", "http-request", new HttpRequestHandler(config.http, parent, route))
      ctx.fireChannelRead(msg)
    case None => notFound(ctx)
  }

  private def doHandshake(ctx: ChannelHandlerContext, msg: HttpRequest): Unit = config.ws.routes(msg) match {
    case Some(route) =>
      ctx.pipeline.addAfter(ctx.name, "ws-handshake", new WsHandshakeHandler(config.ws, parent, route))
      ctx.fireChannelRead(msg)
    case None => notFound(ctx)
  }

  private def notFound(ctx: ChannelHandlerContext): Unit = {
    ctx.writeAndFlush(NotFound.retain()).addListener(ChannelFutureListener.CLOSE)
  }
}
