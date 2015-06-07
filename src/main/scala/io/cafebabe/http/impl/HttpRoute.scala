package io.cafebabe.http.impl

import akka.actor.ActorPath

/**
 * @author Vladimir Konstantinov
 * @version 1.0 (6/8/2015)
 */
case class HttpRoute(uriPath: String, actorPath: ActorPath)
