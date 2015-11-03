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

import io.cafebabe.korro.api.http.HttpResponse
import io.cafebabe.korro.api.http.HttpStatus._
import io.cafebabe.korro.api.route.{HttpRoute, Route, WsRoute}
import io.cafebabe.korro.server.actor.HttpRouterActor
import io.cafebabe.korro.util.log.Logging

import akka.actor.ActorContext
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.Config
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel._
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
  extends ChannelInboundHandlerAdapter with Logging {

  import context.dispatcher
  implicit val timeout = Timeout(2 seconds)

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = ctx.flush()

  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = msg match {

    case req: FullHttpRequest if req.getDecoderResult.isSuccess =>

      val path = new URI(req.getUri).getPath

      (context.actorSelection(HttpRouterActor.name) ? path).mapTo[Option[Route]] onComplete {

        case Success(Some(route: HttpRoute)) =>
          ctx.pipeline.addAfter(ctx.name, "http-request", new HttpRequestChannelHandler(config, route))
          ctx.fireChannelRead(req)

        case Success(Some(route: WsRoute)) =>
          ctx.pipeline.addAfter(ctx.name, "ws-handshake", new WsHandshakeChannelHandler(config, route))
          ctx.fireChannelRead(req)

        case Success(None) =>
          req.release()
          ctx.writeAndFlush(NotFound()).addListener(ChannelFutureListener.CLOSE)

        case Failure(error) =>
          log.error(error, "Error while trying to get route.")
          req.release()
          ctx.writeAndFlush(ServerError()).addListener(ChannelFutureListener.CLOSE)
      }

    case req: FullHttpRequest =>
      req.release()
      ctx.writeAndFlush(BadRequest()).addListener(ChannelFutureListener.CLOSE)

    case _ => ctx.fireChannelRead(msg)
  }
}
