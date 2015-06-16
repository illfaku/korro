package io.cafebabe.http.clinet

import io.netty.bootstrap.Bootstrap
import io.netty.channel.{ChannelFuture, ChannelFutureListener}
import io.netty.handler.codec.http.FullHttpRequest

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
  def fireAsync(httpQuery:FullHttpRequest):Future[Any] = {

    val promise = Promise[Any]()

    def writeOpListener = new ChannelFutureListener {

      override def operationComplete(future: ChannelFuture): Unit =
        if (!future.isSuccess) promise.failure(future.cause())
    }

    def connOpListener = new ChannelFutureListener {

      override def operationComplete(future: ChannelFuture): Unit = {

        if (future.isSuccess) {

          val channel = future.channel()
          channel.pipeline().addLast("client-handler", new NettyHttpClientChannelHandler(promise))
          channel.write(httpQuery).addListener(writeOpListener)
        } else {

          promise.failure(future.cause())
        }
      }
    }

    bootstrap.connect(host, port).addListener(connOpListener)
    promise.future
  }
}
