package io.cafebabe.http.impl

import akka.actor.{ActorPath, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import io.cafebabe.http.api.HttpRequest
import io.cafebabe.http.impl.util.ResponseCodec._
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._
import io.netty.handler.codec.http.websocketx.WebSocketFrame

import java.util.concurrent.TimeUnit._

/**
 * @author Vladimir Konstantinov
 * @version 1.0 (4/14/2015)
 */
class HttpChannelHandler(system: ActorSystem, restRoutes: List[HttpRoute], wsRoutes: List[HttpRoute])
  extends SimpleChannelInboundHandler[Any] {

  import system.dispatcher

  private val config = system.settings.config

  private implicit val timeout = Timeout(config.getDuration("http.router.resolveTimeout", SECONDS), SECONDS)

  override def channelRead0(ctx: ChannelHandlerContext, msg: Any): Unit = msg match {
    case req: FullHttpRequest =>
      if (req.getDecoderResult.isSuccess) processHttp(ctx, new DefaultHttpRequest(req))
      else sendHttpResponse(ctx, HttpResponseStatus.BAD_REQUEST)
    case frame: WebSocketFrame => ???
    case _ => ()
  }

  private def processHttp(ctx: ChannelHandlerContext, req: HttpRequest): Unit = req match {
    case RestRoute(path) =>
      system.actorSelection(path).resolveOne
        .flatMap(_ ? req)
        .map(toHttpResponse)
        .recover(toErrorResponse)
        .foreach(ctx.write(_).addListener(ChannelFutureListener.CLOSE))
    case WsRoute(path) =>
      val location = s"ws://${req.headers(HttpHeaders.Names.HOST)}${req.path}"
      ???
    case _ => sendHttpResponse(ctx, HttpResponseStatus.NOT_FOUND)
  }

  private def sendHttpResponse(ctx: ChannelHandlerContext, status: HttpResponseStatus): Unit = {
    ctx.write(new DefaultHttpResponse(HttpVersion.HTTP_1_1, status)).addListener(ChannelFutureListener.CLOSE)
  }

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = ctx.flush()

  object RestRoute {
    def unapply(req: HttpRequest): Option[ActorPath] = {
      restRoutes.find(route => req.path.startsWith(route.uriPath)).map(_.actorPath)
    }
  }

  object WsRoute {
    def unapply(req: HttpRequest): Option[ActorPath] = wsRoutes.find(_.uriPath == req.path).map(_.actorPath)
  }
}
