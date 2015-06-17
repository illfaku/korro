package io.cafebabe.http.clinet

import io.netty.bootstrap.Bootstrap
import io.netty.channel.{ChannelFuture, ChannelFutureListener}
import io.netty.handler.codec.http.{FullHttpResponse, FullHttpRequest}

import scala.concurrent.{Promise, Future}

/**
 * Created by ygintsyak on 17.06.15.
 */
private [clinet] class NettyHttpClient(bootstrap:Bootstrap, host:String, port:Int) {

  /**
   * Performs HTTP query against remote host in asynchronous way
   * @param httpQuery query
   * @return Future
   */
  def fireAsync(httpQuery:FullHttpRequest):Future[FullHttpResponse] = {

    val promise = Promise[FullHttpResponse]()
    println("Firing event stuff")

    lazy val writeOpListener = new ChannelFutureListener {

      override def operationComplete(future: ChannelFuture): Unit = {
        if (!future.isSuccess) promise.failure(future.cause())
      }
    }

    lazy val connOpListener = new ChannelFutureListener {

      override def operationComplete(future: ChannelFuture): Unit = {

        if (future.isSuccess) {
          val channel = future.channel()
          channel.pipeline().addLast("client-handler", new NettyHttpClientChannelHandler(promise))
          channel.writeAndFlush(httpQuery).addListener(writeOpListener)
        } else {
          promise.failure(future.cause())
        }
      }
    }

    bootstrap.connect(host, port).addListener(connOpListener)
    promise.future
  }
}
