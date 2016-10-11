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

import org.oxydev.korro.http.internal.common.ChannelFutureExt
import org.oxydev.korro.http.internal.common.handler.HttpMessageCodec
import org.oxydev.korro.http.internal.server.Keys
import org.oxydev.korro.http.internal.server.route.RouteInfo

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._

/**
 * Modifies channel pipeline according to request type (common HTTP request or WebSocket handshake).
 * Also makes some validation of request and searches for route using provided config
 * (if not found sends response with status code 404).
 */
@Sharable
class HttpChannelHandler extends SimpleChannelInboundHandler[HttpRequest] {

  private val NotFound = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND)

  private val BadRequest = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST)

  override def channelRead0(ctx: ChannelHandlerContext, msg: HttpRequest): Unit = {
    if (!msg.headers.contains(HttpHeaderNames.HOST)) finish(ctx, BadRequest)
    else if (isHandshake(msg)) doHandshake(ctx, msg)
    else doRequest(ctx, msg)
  }

  private def isHandshake(msg: HttpRequest): Boolean = {
    msg.headers.contains(HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE, true) &&
    msg.headers.contains(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET, true)
  }

  private def doRequest(ctx: ChannelHandlerContext, msg: HttpRequest): Unit = {
    val router = ctx.channel.attr(Keys.router).get
    val parent = ctx.channel.attr(Keys.reqParent).get
    router.find(msg) match {
      case Some(route) =>
        if (ctx.pipeline.get("http-codec") == null) {
          ctx.pipeline.addAfter(ctx.name, "http-codec", new HttpMessageCodec(route.instructions.maxContentLength))
        }
        if (ctx.pipeline.get("http-request") != null) {
          ctx.pipeline.remove("http-request")
        }
        ctx.pipeline.addAfter("http-codec", "http-request", new HttpRequestHandler(parent, route))
        ctx.fireChannelRead(msg)
      case None => finish(ctx, NotFound)
    }
  }

  private def doHandshake(ctx: ChannelHandlerContext, msg: HttpRequest): Unit = {
    val router = ctx.channel.attr(Keys.router).get
    val parent = ctx.channel.attr(Keys.wsParent).get
    router.find(msg) match {
      case Some(route) =>
        ctx.pipeline.addAfter(ctx.name, "http-aggregator", new HttpObjectAggregator(8192))
        ctx.pipeline.addAfter("http-aggregator", "ws-handshake", new WsHandshakeHandler(parent, route))
        ctx.fireChannelRead(msg)
      case None => finish(ctx, NotFound)
    }
  }

  private def finish(ctx: ChannelHandlerContext, msg: FullHttpResponse): Unit = {
    ctx.writeAndFlush(msg.retain()).closeChannel()
  }
}
