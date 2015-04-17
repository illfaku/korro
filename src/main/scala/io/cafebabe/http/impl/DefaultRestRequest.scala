package io.cafebabe.http.impl

import io.cafebabe.http.api.{HttpMethod, RestRequest}
import io.cafebabe.http.impl.util.RequestCodec
import io.netty.handler.codec.http.FullHttpRequest

import java.net.URI

import scala.reflect.ClassTag

/**
 * @author Vladimir Konstantinov
 * @version 1.0 (4/14/2015)
 */
class DefaultRestRequest(request: FullHttpRequest) extends RestRequest {
  override val method = HttpMethod.withName(request.getMethod.name)
  override val path = new URI(request.getUri).getPath
  override def as[T: ClassTag]: T = RequestCodec.fromHttpRequest(request)
}
