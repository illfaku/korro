package io.cafebabe.http.impl

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import io.cafebabe.http.impl.util.ResponseCodec._
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.FullHttpRequest

import scala.concurrent.duration._

/**
 * @author Vladimir Konstantinov
 * @version 1.0 (4/14/2015)
 */
class HttpChannelHandler(system: ActorSystem) extends SimpleChannelInboundHandler[FullHttpRequest] {

  private implicit val timeout = Timeout(5 seconds)

  import system.dispatcher

  override def channelRead0(ctx: ChannelHandlerContext, req: FullHttpRequest): Unit = {
    system.actorSelection(system / "rest").resolveOne
      .flatMap(_ ? new DefaultRestRequest(req))
      .map(toHttpResponse)
      .recover(toErrorResponse)
      .foreach(ctx.write(_).addListener(ChannelFutureListener.CLOSE))
  }

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = ctx.flush()
}
