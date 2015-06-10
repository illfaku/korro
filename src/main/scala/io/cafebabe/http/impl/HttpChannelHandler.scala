package io.cafebabe.http.impl

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import io.cafebabe.http.api.{BinaryWsMessage, HttpRequest, TextWsMessage}
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
class HttpChannelHandler(system: ActorSystem, restRouters: List[RestRouter], wsRouters: List[WsRouter])
  extends SimpleChannelInboundHandler[Any] {

  import system.dispatcher

  private val log = LoggerFactory.getLogger(getClass)

  private val config = new WrappedConfig(system.settings.config)

  private implicit val timeout = Timeout(config.findFiniteDuration("http.router.resolveTimeout").getOrElse(10 seconds))

  private var handshaker: Option[WebSocketServerHandshaker] = None

  private var wsRouter: Option[WsRouter] = None

  override def channelRead0(ctx: ChannelHandlerContext, msg: Any): Unit = msg match {
    case req: FullHttpRequest =>
      if (req.getDecoderResult.isSuccess) processHttp(ctx, req)
      else sendHttpResponse(ctx, HttpResponseStatus.BAD_REQUEST)
    case frame: WebSocketFrame => processFrame(ctx, frame)
    case obj => log.warn("Something unpredicted was received: {}.", obj)
  }

  private def processHttp(ctx: ChannelHandlerContext, request: FullHttpRequest): Unit = {
    val req = new DefaultHttpRequest(request)
    req match {
      case RestRoute(router) =>
        system.actorSelection(router.actorPath).resolveOne
          .flatMap(_ ? req) // TODO: separate timeouts
          .map(toHttpResponse)
          .recover(toErrorResponse)
          .foreach(ctx.write(_).addListener(ChannelFutureListener.CLOSE))
      case WsRoute(router) =>
        val location = s"ws://${req.headers(HttpHeaders.Names.HOST)}${req.path}" // TODO: SSL
        val handshakerFactory = new WebSocketServerHandshakerFactory(location, null, true, router.maxFramePayloadLength)
        handshaker = Option(handshakerFactory.newHandshaker(request))
        handshaker foreach { hs =>
          hs.handshake(ctx.channel, request)
          wsRouter = Some(router)
        }
        if (handshaker.isEmpty) sendUnsupportedVersionResponse(ctx.channel).addListener(ChannelFutureListener.CLOSE)
      case _ => sendHttpResponse(ctx, HttpResponseStatus.NOT_FOUND)
    }
  }

  private def processFrame(ctx: ChannelHandlerContext, frame: WebSocketFrame): Unit = frame match {
    case f: CloseWebSocketFrame => handshaker.foreach(_.close(ctx.channel, f.retain()))
    case f: PingWebSocketFrame => ctx.channel.writeAndFlush(new PongWebSocketFrame(f.content.retain()))
    case f: BinaryWebSocketFrame => wsRouter foreach { router =>
      system.actorSelection(router.actorPath) ! new BinaryWsMessage(readBytes(f.content))
    }
    case f: TextWebSocketFrame => wsRouter foreach { router =>
      system.actorSelection(router.actorPath) ! new TextWsMessage(f.text)
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

  object RestRoute {
    def unapply(req: HttpRequest): Option[RestRouter] = restRouters.find(route => req.path.startsWith(route.uriPath))
  }

  object WsRoute {
    def unapply(req: HttpRequest): Option[WsRouter] = wsRouters.find(_.uriPath == req.path)
  }
}
