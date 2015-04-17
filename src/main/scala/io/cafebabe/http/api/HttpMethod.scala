package io.cafebabe.http.api

/**
 * @author Vladimir Konstantinov
 * @version 1.0 (4/17/2015)
 */
object HttpMethod extends Enumeration {
  type HttpMethod = Value
  val CONNECT, DELETE, GET, HEAD, OPTIONS, PATCH, POST, PUT, TRACE = Value
}


