package io.cafebabe.http.impl

import io.cafebabe.http.api.HttpRequest
import io.cafebabe.http.impl.util.StringUtils._
import io.netty.handler.codec.http.{FullHttpRequest, QueryStringDecoder}
import io.netty.util.CharsetUtil

import scala.collection.JavaConversions._
import scala.reflect.ClassTag

/**
 * @author Vladimir Konstantinov
 * @version 1.0 (4/14/2015)
 */
class DefaultHttpRequest(request: FullHttpRequest) extends HttpRequest {

  private val uri = new QueryStringDecoder(request.getUri)

  override val method = request.getMethod.name

  override val path = uri.path

  override val parameters = uri.parameters.toMap map { case (key, value) => key -> value(0) }

  override val headers = request.headers.entries.map(entry => entry.getKey -> entry.getValue).toMap

  override def content[T: ClassTag]: T = toPrimitive(request.content.toString(CharsetUtil.UTF_8))
}
