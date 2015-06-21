package io.cafebabe.http.server.api

/**
 * @author Vladimir Konstantinov
 * @version 1.0 (4/17/2015)
 */
class HttpMethod(name: String) {
  def unapply(req: HttpRequest): Option[HttpRequest] = if (req.method.equalsIgnoreCase(name)) Some(req) else None
}

object GET extends HttpMethod("GET")
object POST extends HttpMethod("POST")
object PUT extends HttpMethod("PUT")
object DELETE extends HttpMethod("DELETE")
object HEAD extends HttpMethod("HEAD")
object CONNECT extends HttpMethod("CONNECT")
object OPTIONS extends HttpMethod("OPTIONS")
object TRACE extends HttpMethod("TRACE")
