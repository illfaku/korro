package io.cafebabe.http.impl

import io.cafebabe.http.api.{HttpMethod, RestRequest}
import io.cafebabe.http.impl.util.StringCodec
import io.netty.handler.codec.http.{FullHttpRequest, QueryStringDecoder}

import scala.collection.JavaConversions._
import scala.reflect._

/**
 * @author Vladimir Konstantinov
 * @version 1.0 (4/14/2015)
 */
class DefaultRestRequest(request: FullHttpRequest) extends RestRequest {

  private val uri = new QueryStringDecoder(request.getUri)

  override val method = request.getMethod.name

  override val path = uri.path

  override val parameters = uri.parameters.toMap map { case (key, value) => key -> value(0) }

  override val headers = request.headers.entries.map(entry => entry.getKey -> entry.getValue).toMap

  override def content[T: ClassTag]: T = StringCodec.fromString(request.content.toString)
}
