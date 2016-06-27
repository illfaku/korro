/*
 * Copyright 2016 Vladimir Konstantinov, Yuriy Gintsyak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.oxydev.korro.http.tools.route

import org.oxydev.korro.http.api.HttpRequest
import org.oxydev.korro.http.api.HttpResponse.Status.NotFound
import org.oxydev.korro.util.lang.Predicate1

import akka.actor.{Actor, ActorRef}

import scala.collection.mutable

/**
 * Actor for handling [[HttpRequest]] messages with routing functionality.
 * Accepts [[HttpRouter#SetRoute]] and [[HttpRouter#UnsetRoute]] commands.
 *
 * <p>If this actor will not process [[HttpRequest]] itself in `receive` method then it will try to find actor
 * that matches this message and forward message to it, otherwise it will send response with status 404 to sender.
 *
 * <p>Note: this trait overrides `unhandled` method, so if you want to override it too do not forget to call
 * `super.unhandled`.
 */
trait HttpRouter extends Actor {

  private val routes = mutable.Map.empty[ActorRef, Predicate1[HttpRequest]]

  /**
   * Returns optional matching route.
   *
   * @param req Request to match.
   */
  protected def findRoute(req: HttpRequest): Option[ActorRef] = routes.find(_._2(req)).map(_._1)

  /**
   * Forwards [[HttpRequest]] to matching route if found, otherwise sends response with status 404 to sender.
   *
   * <p> Note: because of usage of `sender` method inside be sure to use this method only within this actor.
   *
   * @param req Request to route.
   */
  protected def route(req: HttpRequest): Unit = findRoute(req) match {
    case Some(actor) => actor forward req
    case None => sender ! NotFound()
  }

  override def unhandled(message: Any): Unit = message match {
    case HttpRouter.SetRoute(ref, predicate) => routes += (ref -> predicate)
    case HttpRouter.UnsetRoute(ref) => routes -= ref
    case req: HttpRequest => route(req)
    case _ => super.unhandled(message)
  }
}

/**
 * HttpRouter commands.
 */
object HttpRouter {

  /**
   * Command for HttpRouter to set your actor as handler of matched requests.
   *
   * @param ref Actor reference to set.
   * @param predicate Predicate to test requests against.
   */
  case class SetRoute(ref: ActorRef, predicate: Predicate1[HttpRequest])

  /**
   * Command for HttpRouter to remove your actor from handlers list.
   *
   * @param ref Actor reference to unset.
   */
  case class UnsetRoute(ref: ActorRef)
}
