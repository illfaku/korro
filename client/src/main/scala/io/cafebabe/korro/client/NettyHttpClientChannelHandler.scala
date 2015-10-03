package io.cafebabe.korro.client

import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.{FullHttpResponse, HttpObject}

import scala.concurrent.Promise
import scala.util.Try

/**
 * Created by ygintsyak on 15.06.15.
 */
class NettyHttpClientChannelHandler(promise:Promise[FullHttpResponse]) extends SimpleChannelInboundHandler[HttpObject] {

  override def channelRead0(ctx: ChannelHandlerContext, msg: HttpObject): Unit = {

    msg match {
      case resp: FullHttpResponse =>
        // This is just for approach illustration - resp.retain()
        // If we want to use FullHttpResponse outside of channel handler context it
        // is better to increment reference count otherwise there is a chance not to meet
        // response content :D
        promise.complete(Try(resp.retain()))
      case _ =>
        promise.failure(new IllegalStateException("Unexpected state"))
    }
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {

    // TODO: [Y.Gintsyak] need to investigate if it is a valid error handling approach
    super.exceptionCaught(ctx, cause)
    promise.failure(cause)
  }

  override def channelInactive(ctx: ChannelHandlerContext): Unit =  {
    super.channelInactive(ctx)
  }
}
