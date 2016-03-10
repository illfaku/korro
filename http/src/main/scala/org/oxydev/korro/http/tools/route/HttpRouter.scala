/*
 * Copyright (C) 2015, 2016  Vladimir Konstantinov, Yuriy Gintsyak
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
 * that matches this request and forward request to it, otherwise it will send response with status 404 to sender.
 *
 * <p>Note: this trait overrides `unhandled` method, so if you want to override it too do not forget to call
 * `super.unhandled`.
 */
trait HttpRouter extends Actor {

  private val routes = mutable.Map.empty[ActorRef, Predicate1[HttpRequest]]

  /**
   * Forwards [[HttpRequest]] to matching route if found, otherwise sends response with status 404 to sender.
   *
   * <p> Note: because of usage of `sender` method inside be sure to use this method only within this actor.
   *
   * @param req Request to route.
   */
  protected def route(req: HttpRequest): Unit = {
    routes.find(_._2(req)) match {
      case Some((actor, _)) => actor forward req
      case None => sender ! NotFound()
    }
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
   * @param predicate Matcher to test requests against.
   */
  case class SetRoute(ref: ActorRef, predicate: Predicate1[HttpRequest])

  /**
   * Command for HttpRouter to remove your actor from handlers list.
   *
   * @param ref Actor reference to unset.
   */
  case class UnsetRoute(ref: ActorRef)
}
