package io.cafebabe.http.impl

import akka.actor.{ActorPath, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import io.cafebabe.http.api.{BinaryWsMessage, TextWsMessage}
import io.cafebabe.http.impl.util.ResponseCodec._
import io.cafebabe.util.config.WrappedConfig
import io.netty.buffer.ByteBuf
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse
import io.netty.handler.codec.http.websocketx._
import org.slf4j.LoggerFactory

import scala.concurrent.duration._

/**
 * @author Vladimir Konstantinov
 * @version 1.0 (4/14/2015)
 */
class HttpChannelHandler(system: ActorSystem, routes: HttpRoutes) extends SimpleChannelInboundHandler[AnyRef] {

  import system.dispatcher

  private val log = LoggerFactory.getLogger(getClass)

  private val config = new WrappedConfig(system.settings.config)

  private val resolveTimeout = Timeout(config.findFiniteDuration("http.timeout.resolve").getOrElse(10 seconds))

  private val askTimeout = Timeout(config.findFiniteDuration("http.timeout.ask").getOrElse(60 seconds))

  private var handshaker: Option[WebSocketServerHandshaker] = None

  private var wsActorPath: Option[ActorPath] = None

  override def channelRead0(ctx: ChannelHandlerContext, msg: AnyRef): Unit = msg match {
    case req: FullHttpRequest =>
      if (req.getDecoderResult.isSuccess) processHttp(ctx, req)
      else sendHttpResponse(ctx, HttpResponseStatus.BAD_REQUEST)
    case frame: WebSocketFrame => processFrame(ctx, frame)
    case obj => log.warn("Something unpredicted was received: {}.", obj)
  }

  private def processHttp(ctx: ChannelHandlerContext, nettyReq: FullHttpRequest): Unit = {
    val req = new DefaultHttpRequest(nettyReq)
    routes(req) match {
      case RestRoute(_, actorPath) =>
        system.actorSelection(actorPath).resolveOne()(resolveTimeout)
          .flatMap(_.ask(req)(askTimeout))
          .map(toHttpResponse)
          .recover(toErrorResponse)
          .foreach(ctx.write(_).addListener(ChannelFutureListener.CLOSE))
      case WsRoute(_, actorPath, maxFramePayloadLength) =>
        val location = s"ws://${req.headers(HttpHeaders.Names.HOST)}${req.path}" // TODO: SSL
        val handshakerFactory = new WebSocketServerHandshakerFactory(location, null, true, maxFramePayloadLength)
        handshaker = Option(handshakerFactory.newHandshaker(nettyReq))
        handshaker foreach { hs =>
          hs.handshake(ctx.channel, nettyReq).sync()
          wsActorPath = Some(actorPath)
        }
        if (handshaker.isEmpty) sendUnsupportedVersionResponse(ctx.channel).addListener(ChannelFutureListener.CLOSE)
      case NoRoute => sendHttpResponse(ctx, HttpResponseStatus.NOT_FOUND)
    }
  }

  private def processFrame(ctx: ChannelHandlerContext, frame: WebSocketFrame): Unit = frame match {
    case f: CloseWebSocketFrame => handshaker.foreach(_.close(ctx.channel, f.retain()))
    case f: PingWebSocketFrame => ctx.channel.writeAndFlush(new PongWebSocketFrame(f.content.retain()))
    case f: BinaryWebSocketFrame => wsActorPath foreach { actorPath =>
      system.actorSelection(actorPath) ! new BinaryWsMessage(readBytes(f.content))
    }
    case f: TextWebSocketFrame => wsActorPath foreach { actorPath =>
      system.actorSelection(actorPath) ! new TextWsMessage(f.text)
    }
    case obj => log.warn("Some unpredicted frame was received: {}.", obj)
  }

  private def readBytes(buf: ByteBuf): Array[Byte] = {
    val bytes = new Array[Byte](buf.readableBytes)
    buf.readBytes(bytes)
    bytes
  }

  private def sendHttpResponse(ctx: ChannelHandlerContext, status: HttpResponseStatus): Unit = {
    ctx.write(new DefaultHttpResponse(HttpVersion.HTTP_1_1, status)).addListener(ChannelFutureListener.CLOSE)
  }

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = ctx.flush()

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    log.error("Something unthinkable happened while processing http request.", cause)
    ctx.close()
  }
}
