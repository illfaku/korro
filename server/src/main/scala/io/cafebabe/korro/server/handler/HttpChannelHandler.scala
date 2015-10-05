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

import io.cafebabe.korro.api.http.route.{GetRoute, HttpRoute, Route, WsRoute}
import io.cafebabe.korro.server.actor.HttpRouterActor

import akka.actor.ActorContext
import akka.pattern.ask
import akka.util.Timeout
import io.netty.channel._
import io.netty.handler.codec.http._
import org.slf4j.LoggerFactory

import java.net.URI

import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class HttpChannelHandler(port: Int)(implicit context: ActorContext) extends ChannelInboundHandlerAdapter {

  private val log = LoggerFactory.getLogger(getClass)

  import context.dispatcher
  implicit val timeout = Timeout(2 seconds)

  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = msg match {
    case req: FullHttpRequest if req.getDecoderResult.isSuccess =>
      val path = new URI(req.getUri).getPath
      (HttpRouterActor.selection ? GetRoute(port, path)).mapTo[Option[Route]] onComplete {
        case Success(Some(route: HttpRoute)) => ctx.fireChannelRead(RoutedHttpRequest(req, route))
        case Success(Some(route: WsRoute)) => ctx.fireChannelRead(RoutedWsHandshake(req, route))
        case Success(None) =>
          req.release()
          sendHttpResponse(ctx, HttpResponseStatus.NOT_FOUND)
        case Failure(error) =>
          req.release()
          log.error("Error while trying to get route.", error)
          sendHttpResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR)
      }
    case req: FullHttpRequest => sendHttpResponse(ctx, HttpResponseStatus.BAD_REQUEST)
    case _ => ctx.fireChannelRead(msg)
  }

  private def sendHttpResponse(ctx: ChannelHandlerContext, status: HttpResponseStatus): Unit = {
    sendHttpResponse(ctx, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status))
  }

  private def sendHttpResponse(ctx: ChannelHandlerContext, res: FullHttpResponse): Unit = {
    ctx.writeAndFlush(res).addListener(ChannelFutureListener.CLOSE)
  }

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = ctx.flush()
}
