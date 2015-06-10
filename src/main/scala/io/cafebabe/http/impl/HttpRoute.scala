package io.cafebabe.http.impl

import akka.actor.ActorPath
import io.cafebabe.http.api.HttpRequest

/**
 * @author Vladimir Konstantinov
 * @version 1.0 (6/10/2015)
 */
sealed trait HttpRoute

case class RestRoute(uriPath: String, actorPath: ActorPath) extends HttpRoute

case class WsRoute(uriPath: String, actorPath: ActorPath, maxFramePayloadLength: Int) extends HttpRoute

case object NoRoute extends HttpRoute

class HttpRoutes(restRoutes: List[RestRoute], wsRoutes: List[WsRoute]) {
  def apply(req: HttpRequest): HttpRoute = {
    restRoutes.find(route => req.path.startsWith(route.uriPath))
      .orElse(wsRoutes.find(_.uriPath == req.path))
      .getOrElse(NoRoute)
  }
}
