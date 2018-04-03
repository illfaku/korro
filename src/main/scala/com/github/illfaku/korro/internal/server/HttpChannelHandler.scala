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

import com.github.illfaku.korro.internal.common.HttpActorFactory.NewHttpActor
import com.github.illfaku.korro.internal.common.{ChannelFutureExt, HttpLoggingHandler}
import com.github.illfaku.korro.internal.server.Routing.{DstRoute, FindRoute, NoRoute, Route}
import com.github.illfaku.korro.util.logging.Logging

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._

import scala.concurrent.Await
import scala.concurrent.duration._

private[server] class HttpChannelHandler(parent: ActorRef)
  extends SimpleChannelInboundHandler[HttpRequest] with Logging {

  private implicit val timeout = Timeout(5 seconds)

  private val NotFound = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND)

  private val BadRequest = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST)

  override def channelRead0(ctx: ChannelHandlerContext, msg: HttpRequest): Unit = {
    if (msg.decoderResult.isFailure) {
      log.debug("Korro | Failure in request decoding: {}", msg.decoderResult.cause)
      ctx.writeAndFlush(BadRequest.retainedDuplicate).closeChannel()
    } else {
      Await.result((parent ? FindRoute(msg)).mapTo[Route], Duration.Inf) match {

        case route: DstRoute =>
          ctx.pipeline.remove(classOf[HttpLoggingHandler])
          ctx.pipeline.addLast("korro-logger", new HttpLoggingHandler(route.instructions))
          ctx.pipeline.addLast("netty-http-aggregator", new HttpObjectAggregator(route.instructions.maxContentLength))
          ctx.pipeline.addLast("korro-request-decoder", HttpRequestDecoder)
          ctx.pipeline.addLast("korro-response-encoder", HttpResponseEncoder)
          ctx.pipeline.addLast("korro-request-handler", new HttpRequestHandler(route)(
            Await.result((parent ? NewHttpActor(ctx.channel)).mapTo[ActorRef], Duration.Inf)
          ))
          ctx.fireChannelRead(msg)

        case NoRoute =>
          ctx.writeAndFlush(NotFound.retainedDuplicate).closeChannel()
      }
    }
  }

  private def isHandshake(msg: HttpRequest): Boolean = {
    msg.headers.contains(HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE, true) &&
    msg.headers.contains(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET, true)
  }
}
