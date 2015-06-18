package io.cafebabe.http.client.example

import java.util.concurrent.Executors

import io.cafebabe.http.client.{NettyHttpClient, NettyHttpClientChannelInitializer}
import io.netty.bootstrap.Bootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.HttpHeaders
import io.netty.util.CharsetUtil

import scala.concurrent.ExecutionContext
import scala.util.Success

/**
 * Created by ygintsyak on 17.06.15.
 */
object TestGetQuery {

  implicit val ec = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(1))

  // Okay, lets see if I can request something from http://example.org
  def main(agrs:Array[String]) = {

    val bootstrap = new Bootstrap()
      .group(new NioEventLoopGroup())
      .channel(classOf[NioSocketChannel])
      .handler(new NettyHttpClientChannelInitializer())

    val c = new NettyHttpClient(bootstrap, "example.org", 80)


    println("Firing new request")
    val request = new TestEntity("value1", "value2").nettyHttpRequest()
    request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE)
    request.headers().set(HttpHeaders.Names.HOST, "example.org")

    println(request.toString)
    c.fireAsync(request) onComplete {
      case Success(resp) => try {
        println(s"Success response ${resp.getStatus}")
        val content = resp.content()
        println(s"${content.toString(CharsetUtil.UTF_8)}")
      } finally {
        println(s"Releasing response: ${resp.release()}")
      }
      case _ => println("On complete")
    }
  }
}
