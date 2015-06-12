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
package io.cafebabe.http.impl

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import io.cafebabe.http.api.{BinaryWsMessage, ConnectWsMessage, TextWsMessage}
import io.cafebabe.http.impl.util.ByteBufUtils.toBytes
import io.cafebabe.http.impl.util.ResponseCodec._
import io.cafebabe.util.config.WrappedConfig
import io.netty.channel.{Channel, ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse
import io.netty.handler.codec.http.websocketx._
import org.slf4j.LoggerFactory

import java.net.{InetSocketAddress, URI}
import java.util.UUID

import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
 * @author Vladimir Konstantinov
 * @version 1.0 (4/14/2015)
 */
class HttpChannelHandler(system: ActorSystem, routes: HttpRoutes) extends SimpleChannelInboundHandler[AnyRef] {

  import system.dispatcher

  private val log = LoggerFactory.getLogger(getClass)

  private val config = new WrappedConfig(system.settings.config)

  private val resolveTimeout = config.findFiniteDuration("http.timeout.resolve").getOrElse(10 seconds)

  private val askTimeout = Timeout(config.findFiniteDuration("http.timeout.ask").getOrElse(60 seconds))

  private var handshaker: WebSocketServerHandshaker = null

  private var wsReceiver: ActorRef = null

  private var wsSender: ActorRef = null

  override def channelRead0(ctx: ChannelHandlerContext, msg: AnyRef): Unit = msg match {
    case req: FullHttpRequest =>
      if (req.getDecoderResult.isSuccess) processHttp(ctx, req)
      else sendHttpResponse(ctx, HttpResponseStatus.BAD_REQUEST)
    case frame: WebSocketFrame => processFrame(ctx, frame)
    case obj => log.warn("Something unpredicted was received: {}.", obj)
  }

  private def processHttp(ctx: ChannelHandlerContext, nettyReq: FullHttpRequest): Unit = {
    routes(new URI(nettyReq.getUri).getPath) match {
      case RestRoute(_, actorPath) =>
        system.actorSelection(actorPath).resolveOne(resolveTimeout)
          .flatMap(_.ask(new DefaultHttpRequest(nettyReq))(askTimeout))
          .map(toHttpResponse)
          .recover(toErrorResponse)
          .foreach(ctx.write(_).addListener(ChannelFutureListener.CLOSE))
      case route: WsRoute => handshake(ctx, nettyReq, route)
      case NoRoute => sendHttpResponse(ctx, HttpResponseStatus.NOT_FOUND)
    }
  }

  private def handshake(ctx: ChannelHandlerContext, req: FullHttpRequest, route: WsRoute): Unit = {
    val location = s"ws://${req.headers.get(HttpHeaders.Names.HOST)}${route.uriPath}" // TODO: SSL
    val handshakerFactory = new WebSocketServerHandshakerFactory(location, null, true, route.maxFramePayloadLength)
    handshaker = handshakerFactory.newHandshaker(req)
    if (handshaker != null) {
      handshaker.handshake(ctx.channel, req).sync()
      system.actorSelection(route.actorPath).resolveOne(resolveTimeout) onComplete {
        case Success(actorRef) =>
          wsReceiver = actorRef
          wsSender = system.actorOf(Props(new WsMessageSender(ctx.channel)), "ws-" + UUID.randomUUID)
          wsReceiver.tell(new ConnectWsMessage(extractHost(ctx.channel, req)), wsSender)
        case Failure(error) => sendHttpResponse(ctx, toErrorResponse(error))
      }
    } else sendUnsupportedVersionResponse(ctx.channel).addListener(ChannelFutureListener.CLOSE)
  }

  private def extractHost(ch: Channel, req: FullHttpRequest): String = {
    val host = req.headers().get("X-Real-IP")
    if (host != null && host.nonEmpty) host
    else ch.remoteAddress.asInstanceOf[InetSocketAddress].getHostString
  }

  private def processFrame(ctx: ChannelHandlerContext, frame: WebSocketFrame): Unit = frame match {
    case f: CloseWebSocketFrame => handshaker.close(ctx.channel, f.retain())
    case f: PingWebSocketFrame => ctx.channel.writeAndFlush(new PongWebSocketFrame(f.content.retain()))
    case f: BinaryWebSocketFrame =>
      wsReceiver.tell(new BinaryWsMessage(toBytes(f.content)), wsSender)
    case f: TextWebSocketFrame =>
      wsReceiver.tell(new TextWsMessage(f.text), wsSender)
    case obj => log.warn("Some unpredicted frame was received: {}.", obj)
  }

  private def sendHttpResponse(ctx: ChannelHandlerContext, status: HttpResponseStatus): Unit = {
    sendHttpResponse(ctx, new DefaultHttpResponse(HttpVersion.HTTP_1_1, status))
  }

  private def sendHttpResponse(ctx: ChannelHandlerContext, res: HttpResponse): Unit = {
    ctx.write(res).addListener(ChannelFutureListener.CLOSE)
  }

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = ctx.flush()

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    log.error("Something unthinkable happened while processing http request.", cause)
    ctx.close()
  }
}
