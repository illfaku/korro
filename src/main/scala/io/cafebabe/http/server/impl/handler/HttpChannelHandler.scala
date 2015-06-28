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
package io.cafebabe.http.server.impl.handler

import io.cafebabe.http.server.api.{ConnectWsMessage, HttpResponse}
import io.cafebabe.http.server.impl.convert.{HttpRequestConverter, HttpResponseConverter}

import akka.actor.ActorSystem
import akka.pattern.ask
import io.netty.channel.{Channel, ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse
import io.netty.handler.codec.http.websocketx._
import org.slf4j.LoggerFactory

import java.net.{InetSocketAddress, URI}

import scala.util.{Failure, Success}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class HttpChannelHandler(actors: ActorSystem, routes: Routes) extends SimpleChannelInboundHandler[FullHttpRequest] {

  import actors.dispatcher

  private val log = LoggerFactory.getLogger(getClass)

  override def channelRead0(ctx: ChannelHandlerContext, req: FullHttpRequest): Unit = {
      if (req.getDecoderResult.isSuccess) {
        val path = new URI(req.getUri).getPath
        routes(path) match {
          case route: HttpRoute => request(ctx, req, route)
          case route: WsRoute => handshake(ctx, req, route)
          case NoRoute => sendHttpResponse(ctx, HttpResponseStatus.NOT_FOUND)
        }
      } else sendHttpResponse(ctx, HttpResponseStatus.BAD_REQUEST)
  }

  private def request(ctx: ChannelHandlerContext, req: FullHttpRequest, route: HttpRoute): Unit = {
    actors.actorSelection(route.actor).resolveOne(route.resolveTimeout)
      .flatMap(_.ask(HttpRequestConverter.fromNetty(req, route.path))(route.requestTimeout))
      .mapTo[HttpResponse]
      .map(HttpResponseConverter.toNetty)
      .recover(HttpResponseConverter.toError)
      .foreach(sendHttpResponse(ctx, _))
  }

  private def handshake(ctx: ChannelHandlerContext, req: FullHttpRequest, route: WsRoute): Unit = {
    val location = s"ws://${req.headers.get(HttpHeaders.Names.HOST)}${route.path}"
    val handshakerFactory = new WebSocketServerHandshakerFactory(location, null, true)
    val handshaker = handshakerFactory.newHandshaker(req)
    if (handshaker != null) {
      actors.actorSelection(route.actor).resolveOne(route.resolveTimeout) onComplete {
        case Success(receiver) =>
          val sender = actors.actorOf(WsMessageSender.props(ctx.channel), WsMessageSender.name)
          val host = extractHost(ctx.channel, req)
          ctx.channel.pipeline.remove(this).addLast(new WsChannelHandler(host, receiver, sender))
          handshaker.handshake(ctx.channel, req).sync()
          receiver.tell(new ConnectWsMessage(host), sender)
        case Failure(error) => sendHttpResponse(ctx, HttpResponseConverter.toError(error))
      }
    } else sendUnsupportedVersionResponse(ctx.channel).addListener(ChannelFutureListener.CLOSE)
  }

  private def extractHost(ch: Channel, req: FullHttpRequest): String = {
    val host = req.headers().get("X-Real-IP")
    if (host != null && host.trim.nonEmpty) host
    else ch.remoteAddress match {
      case address: InetSocketAddress => address.getHostString
      case _ => "UNKNOWN"
    }
  }

  private def sendHttpResponse(ctx: ChannelHandlerContext, status: HttpResponseStatus): Unit = {
    sendHttpResponse(ctx, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status))
  }

  private def sendHttpResponse(ctx: ChannelHandlerContext, res: FullHttpResponse): Unit = {
    ctx.write(res).addListener(ChannelFutureListener.CLOSE)
  }

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = ctx.flush()

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    log.error("Something unthinkable happened while processing http request.", cause)
    ctx.close()
  }
}
