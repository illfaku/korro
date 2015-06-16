package io.cafebabe.http.clinet

import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.{HttpContent, HttpResponse, HttpObject}

/**
 * Created by ygintsyak on 15.06.15.
 */
class HttpResponseChannelHandler extends SimpleChannelInboundHandler[HttpObject] {


  override def channelRead0(ctx: ChannelHandlerContext, msg: HttpObject): Unit = {

    if (msg.isInstanceOf[HttpResponse]) {




    }
    if (msg.isInstanceOf[HttpContent]) {


    }
  }
}
