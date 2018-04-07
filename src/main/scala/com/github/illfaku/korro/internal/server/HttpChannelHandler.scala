/*
 * Copyright 2018 Vladimir Konstantinov
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
package com.github.illfaku.korro.internal.server

import com.github.illfaku.korro.dto.HttpRequest.Uri
import com.github.illfaku.korro.dto.ws.{WsHandshakeRequest, WsHandshakeResponse}
import com.github.illfaku.korro.internal.common.HttpActorFactory.NewHttpActor
import com.github.illfaku.korro.internal.common.{ChannelFutureExt, HttpHeadersCodec, HttpInstructions, HttpLoggingHandler, WsMessageCodec}
import com.github.illfaku.korro.internal.server.Routing.{DstRoute, FindRoute, NoRoute, Route}
import com.github.illfaku.korro.util.logging.Logging

import akka.actor.{ActorRef, PoisonPill}
import akka.pattern.ask
import akka.util.Timeout
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._
import io.netty.handler.codec.http.websocketx.{WebSocketFrameAggregator, WebSocketServerProtocolHandler}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}

private[server] class HttpChannelHandler(parent: ActorRef, defaultInstructions: HttpInstructions)
  extends SimpleChannelInboundHandler[HttpRequest] with Logging {

  private implicit val timeout = Timeout(5 seconds)

  private val NotFound = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND)

  private val BadRequest = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST)

  private val ServerError = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR)

  override def channelRead0(ctx: ChannelHandlerContext, msg: HttpRequest): Unit = {
    if (msg.decoderResult.isFailure) {
      log.debug("Korro | Failure in request decoding: {}", msg.decoderResult.cause)
      addLogging(ctx, msg)
      ctx.writeAndFlush(BadRequest.retainedDuplicate).closeChannel()
    } else {
      Await.result((parent ? FindRoute(msg)).mapTo[Route], Duration.Inf) match {
        case route: DstRoute =>
          modifyPipeline(ctx, msg, route)
          ctx.fireChannelRead(msg)
        case NoRoute =>
          addLogging(ctx, msg)
          ctx.writeAndFlush(NotFound.retainedDuplicate).closeChannel()
      }
    }
  }

  private def modifyPipeline(ctx: ChannelHandlerContext, msg: HttpRequest, route: DstRoute): Unit = {
    ctx.pipeline.addLast("korro-logger", HttpLoggingHandler(route.instructions, ctx.channel))
    ctx.pipeline.addLast("netty-http-aggregator", new HttpObjectAggregator(route.instructions.maxContentLength))
    if (isHandshake(msg)) {
      val outActor = Await.result((parent ? NewHttpActor(ctx.channel)).mapTo[ActorRef], Duration.Inf)
      val handshakeRequest = WsHandshakeRequest(outActor, Uri(msg.uri), HttpHeadersCodec.decode(msg.headers))
      Await.ready((route ? handshakeRequest).mapTo[WsHandshakeResponse], Duration.Inf).value.get match {
        case Success(WsHandshakeResponse(inActor)) =>
          ctx.pipeline.addLast(new WebSocketServerProtocolHandler(
            msg.uri, null, false, route.instructions.maxWsFramePayloadLength
          ))
          ctx.pipeline.addLast("netty-ws-aggregator", new WebSocketFrameAggregator(route.instructions.maxContentLength))
          ctx.pipeline.addLast("korro-ws-codec", WsMessageCodec)
          ctx.pipeline.addLast("korro-ws-handler", new WsChannelHandler(outActor, inActor))
          ctx.pipeline.remove(this)
        case Failure(cause) =>
          outActor ! PoisonPill
          exceptionCaught(ctx, cause)
      }
    } else {
      ctx.pipeline.addLast("korro-request-decoder", HttpRequestDecoder)
      ctx.pipeline.addLast("korro-response-encoder", HttpResponseEncoder)
      ctx.pipeline.addLast("korro-request-handler", new HttpRequestHandler(route)(
        Await.result((parent ? NewHttpActor(ctx.channel)).mapTo[ActorRef], Duration.Inf)
      ))
    }
  }

  private def addLogging(ctx: ChannelHandlerContext, msg: HttpRequest): Unit = {
    val handler = HttpLoggingHandler(defaultInstructions, ctx.channel)
    handler.logRead(msg)
    ctx.pipeline.addBefore(ctx.name, "korro-logger", handler)
  }

  private def isHandshake(msg: HttpRequest): Boolean = {
    msg.headers.contains(HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE, true) &&
    msg.headers.contains(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET, true)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) = {
    log.error(cause, "Korro | Server failure.")
    ctx.writeAndFlush(ServerError.retainedDuplicate).closeChannel()
  }
}
