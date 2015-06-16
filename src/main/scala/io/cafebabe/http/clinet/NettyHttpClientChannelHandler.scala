package io.cafebabe.http.clinet

import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.{FullHttpResponse, HttpContent, HttpResponse, HttpObject}

import scala.concurrent.{Promise, Future}
import scala.util.Try

/**
 * Created by ygintsyak on 15.06.15.
 */
class NettyHttpClientChannelHandler(promise:Promise[FullHttpResponse]) extends SimpleChannelInboundHandler[HttpObject] {

  override def channelRead0(ctx: ChannelHandlerContext, msg: HttpObject): Unit = msg match {

    case resp: FullHttpResponse =>
      promise.complete(Try(resp))
    case _ =>
      promise.failure(new IllegalStateException("Unexpected state"))
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {

    // TODO: [Y.Gintsyak] need to investigate if it is a valid error handling approach
    super.exceptionCaught(ctx, cause)
    promise.failure(cause)
  }
}
