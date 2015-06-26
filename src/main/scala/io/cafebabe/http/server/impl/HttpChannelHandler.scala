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
package io.cafebabe.http.server.impl

import akka.actor.ActorSystem
import akka.pattern.ask
import io.cafebabe.http.server.api.{ConnectWsMessage, HttpResponse}
import io.cafebabe.http.server.impl.util.HttpResponseConverter
import io.cafebabe.util.config.wrapped
import io.netty.channel.{Channel, ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse
import io.netty.handler.codec.http.websocketx._
import io.netty.handler.codec.http.{HttpResponse => _, _}
import org.slf4j.LoggerFactory

import java.net.{InetSocketAddress, URI}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 * @version 1.0 (4/14/2015)
 */
class HttpChannelHandler(system: ActorSystem, routes: HttpRoutes) extends SimpleChannelInboundHandler[FullHttpRequest] {

  import system.dispatcher

  private val log = LoggerFactory.getLogger(getClass)

  private val config = wrapped(system.settings.config.getConfig("cafebabe.http.server"))

  private val resolveTimeout = config.findFiniteDuration("timeout.resolve").getOrElse(10 seconds)

  private val askTimeout = config.findFiniteDuration("timeout.ask").getOrElse(60 seconds)

  override def channelRead0(ctx: ChannelHandlerContext, req: FullHttpRequest): Unit = {
      if (req.getDecoderResult.isSuccess) {
        routes(new URI(req.getUri).getPath) match {
          case route: RestRoute => request(ctx, req, route)
          case route: WsRoute => handshake(ctx, req, route)
          case NoRoute => sendHttpResponse(ctx, HttpResponseStatus.NOT_FOUND)
        }
      } else sendHttpResponse(ctx, HttpResponseStatus.BAD_REQUEST)
  }

  private def request(ctx: ChannelHandlerContext, req: FullHttpRequest, route: RestRoute): Unit = {
    system.actorSelection(route.actorPath).resolveOne(resolveTimeout)
      .flatMap(_.ask(HttpRequestConverter.fromNetty(req, route.uriPath))(askTimeout))
      .mapTo[HttpResponse]
      .map(HttpResponseConverter.toNetty)
      .recover(HttpResponseConverter.toError)
      .foreach(sendHttpResponse(ctx, _))
  }

  private def handshake(ctx: ChannelHandlerContext, req: FullHttpRequest, route: WsRoute): Unit = {
    val location = s"ws://${req.headers.get(HttpHeaders.Names.HOST)}${route.uriPath}" // TODO: SSL
    val handshakerFactory = new WebSocketServerHandshakerFactory(location, null, true, route.maxFramePayloadLength)
    val handshaker = handshakerFactory.newHandshaker(req)
    if (handshaker != null) {
      system.actorSelection(route.actorPath).resolveOne(resolveTimeout) onComplete {
        case Success(receiver) =>
          val sender = system.actorOf(WsMessageSender.props(ctx.channel), WsMessageSender.name)
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
