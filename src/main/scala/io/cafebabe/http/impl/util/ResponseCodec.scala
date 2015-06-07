package io.cafebabe.http.impl.util

import akka.actor.ActorNotFound
import akka.pattern.AskTimeoutException
import io.cafebabe.http.api.exception.{BadRequestException, NotFoundException}
import io.netty.buffer.Unpooled
import io.netty.handler.codec.http._
import org.slf4j.LoggerFactory

import java.util.UUID

/**
 * @author Vladimir Konstantinov
 * @version 1.0 (4/12/2015)
 */
object ResponseCodec {

  private val log = LoggerFactory.getLogger(getClass)

  def toHttpResponse(response: Any): FullHttpResponse = {
    newHttpResponse(HttpResponseStatus.OK, response.toString)
  }

  val toErrorResponse: PartialFunction[Throwable, FullHttpResponse] = {
    case e: ActorNotFound => newHttpResponse(HttpResponseStatus.SERVICE_UNAVAILABLE, "Service unavailable.")
    case e: AskTimeoutException => newHttpResponse(HttpResponseStatus.REQUEST_TIMEOUT, "Request timeout.")
    case e: NotFoundException => newHttpResponse(HttpResponseStatus.NOT_FOUND, "Not found.")
    case e: BadRequestException => newHttpResponse(HttpResponseStatus.BAD_REQUEST, "Bad request.")
    case e: Throwable =>
      val message = s"Internal Error #${UUID.randomUUID}."
      log.error(message, e)
      newHttpResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, message)
  }

  private def newHttpResponse(status: HttpResponseStatus, content: String): FullHttpResponse = {
    val buf = Unpooled.wrappedBuffer(content.getBytes)
    val result = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, buf)
    result.headers.set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=utf-8")
    result.headers.set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE)
    result
  }
}
