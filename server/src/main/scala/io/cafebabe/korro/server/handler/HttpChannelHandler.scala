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

import io.cafebabe.korro.api.route.{HttpRoute, Route, WsRoute}
import io.cafebabe.korro.server.actor.HttpRouterActor
import io.cafebabe.korro.util.log.Logging

import akka.actor.ActorContext
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.Config
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._

import java.net.URI

import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
@Sharable
class HttpChannelHandler(config: Config)(implicit context: ActorContext)
  extends SimpleChannelInboundHandler[HttpRequest] with Logging {

  import context.dispatcher
  implicit val timeout = Timeout(2 seconds)

  override def channelRead0(ctx: ChannelHandlerContext, msg: HttpRequest): Unit = {

    val path = new URI(msg.getUri).getPath

    (context.actorSelection(HttpRouterActor.name) ? path).mapTo[Option[Route]] onComplete {

      case Success(Some(route: HttpRoute)) =>
        ctx.pipeline.addAfter("korro-decoder", "http-request", new HttpRequestChannelHandler(config, route))
        ctx.fireChannelRead(msg)

      case Success(Some(route: WsRoute)) =>
        ctx.pipeline.addAfter(ctx.name, "ws-handshake", new WsHandshakeChannelHandler(config, route))
        ctx.fireChannelRead(msg)

      case Success(None) => sendResponse(ctx, HttpResponseStatus.NOT_FOUND)

      case Failure(error) =>
        log.error(error, "Error while trying to get route.")
        sendResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR)
    }
  }

  private def sendResponse(ctx: ChannelHandlerContext, status: HttpResponseStatus): Unit = {
    ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status)).addListener(ChannelFutureListener.CLOSE)
  }
}
