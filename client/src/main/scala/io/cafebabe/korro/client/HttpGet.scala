package io.cafebabe.korro.client

import io.netty.handler.codec.http._

/**
 * Created by ygintsyak on 19.06.15.
 */
trait HttpGet extends HttpRequestNature {

  override def nettyHttpRequest():FullHttpRequest = {

    val query = new QueryStringEncoder(uri, charset)
    params foreach { x => query.addParam(x._1, x._2) }

    val req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, query.toString)
    headers foreach { x => req.headers().add(x._1, x._2)}
    req
  }
}
