package io.cafebabe.korro.client

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.{HttpClientCodec, HttpContentDecompressor, HttpObjectAggregator}

/**
 * Created by ygintsyak on 16.06.15.
 */
class NettyHttpClientChannelInitializer extends ChannelInitializer[SocketChannel] {

  override def initChannel(ch: SocketChannel): Unit = {

    val pipeline = ch.pipeline()
    pipeline.addLast("http-codec", new HttpClientCodec())
    pipeline.addLast("http-decompressor", new HttpContentDecompressor())
    pipeline.addLast("http-aggregator", new HttpObjectAggregator(65536))
  }
}
