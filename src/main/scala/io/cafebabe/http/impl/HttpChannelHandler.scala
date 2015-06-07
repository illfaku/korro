package io.cafebabe.http.impl

import akka.actor.{ActorPath, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import io.cafebabe.http.impl.util.ResponseCodec._
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.FullHttpRequest

import java.util.concurrent.TimeUnit._

/**
 * @author Vladimir Konstantinov
 * @version 1.0 (4/14/2015)
 */
class HttpChannelHandler(system: ActorSystem, routerPath: ActorPath)
  extends SimpleChannelInboundHandler[FullHttpRequest] {

  private val config = system.settings.config

  private implicit val timeout = Timeout(config.getDuration("http.router.resolveTimeout", SECONDS), SECONDS)

  import system.dispatcher

  override def channelRead0(ctx: ChannelHandlerContext, req: FullHttpRequest): Unit = {
    system.actorSelection(routerPath).resolveOne
      .flatMap(_ ? new DefaultHttpRequest(req))
      .map(toHttpResponse)
      .recover(toErrorResponse)
      .foreach(ctx.write(_).addListener(ChannelFutureListener.CLOSE))
  }

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = ctx.flush()
}
