package io.cafebabe.korro.client

import io.netty.handler.codec.http.{FullHttpRequest, HttpConstants}

import java.nio.charset.Charset

/**
 * Created by ygintsyak on 19.06.15.
 */
trait HttpRequestNature {

  def uri:String = "/"

  def params:Iterable[(String,String)] = Iterable.empty

  def headers:Iterable[(String,AnyRef)] = Iterable.empty

  def charset:Charset = HttpConstants.DEFAULT_CHARSET

  /**
   *
   * @return
   */
  def nettyHttpRequest:FullHttpRequest
}
