package io.cafebabe.http.client

import java.nio.charset.Charset

import io.netty.handler.codec.http.{HttpConstants, FullHttpRequest}

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
