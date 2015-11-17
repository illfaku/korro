package io.cafebabe.korro.api.http

import io.cafebabe.korro.api.http.HttpParams.HttpParams

/**
  * TODO: Add description.
  *
  * @author Vladimir Konstantinov
  */
trait HttpMessage {
  def content: HttpContent
  def headers: HttpParams
}

/**
  * HTTP request representation. It contains:
  * <ul>
  *   <li>method
  *   <li>path without prefix
  *   <li>query parameters from uri or body (if Content-Type is application/x-www-form-urlencoded)
  *   <li>content (body)
  *   <li>headers
  *
  * @author Vladimir Konstantinov
  */
class HttpRequest(
  val method: String,
  val path: String,
  val parameters: HttpParams,
  val content: HttpContent,
  val headers: HttpParams
) extends HttpMessage

/**
  * HTTP response with status code, content and headers.
  *
  * @author Vladimir Konstantinov
  */
class HttpResponse(
  val status: Int,
  val content: HttpContent,
  val headers: HttpParams
) extends HttpMessage
