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
package org.oxydev.korro.http.api.actor

import org.oxydev.korro.http.api.{HttpStatus, HttpRequest}

import akka.actor.{Actor, ActorRef}

import scala.collection.mutable

/**
 * Actor for handling `HttpRequest` messages just like `HttpActor` but with routing functionality.
 *
 * <p>Send `HttpRouter.SetRoute(ActorRef, RouteMatcher)` to it to set your actor as handler of matched requests.
 * Send `HttpRouter.UnsetRoute(ActorRef)` to remove your actor from handlers list.
 *
 * <p>If this actor will not process `HttpRequest` itself in `receive` method then it will try to find handler-actor
 * that matches this request and forward request to it, otherwise it will send `HttpStatus.NotFound()` to the sender.
 *
 * <p>Note: this trait overrides `unhandled` method, so if you want to override it too do not forget to call
 * `super.unhandled`.
 *
 * @author Vladimir Konstantinov
 */
trait HttpRouter extends Actor {

  private val routes = mutable.Map.empty[ActorRef, RouteMatcher]

  override def unhandled(message: Any): Unit = message match {
    case HttpRouter.SetRoute(actor, matcher) => routes += (actor -> matcher)
    case HttpRouter.UnsetRoute(actor) => routes -= actor
    case req: HttpRequest => routes.find(_._2(req)) match {
      case Some((actor, _)) => actor forward req
      case None => sender ! HttpStatus.NotFound()
    }
    case _ => super.unhandled(message)
  }
}

/**
 * Companion object for `HttpRouter` trait.
 */
object HttpRouter {

  /**
   * Message for router actor to set your actor as handler of matched requests.
    *
    * @param actor actor reference of your handler-actor
   * @param matcher matcher to test requests against
   */
  case class SetRoute(actor: ActorRef, matcher: RouteMatcher)

  /**
    * Message for router actor to remove your actor from handlers list.
    *
    * @param actor actor reference of your handler-actor
    */
  case class UnsetRoute(actor: ActorRef)
}


/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
trait RouteMatcher { self =>

  def apply(req: HttpRequest): Boolean

  def &&(other: RouteMatcher): RouteMatcher = new RouteMatcher {
    override def apply(req: HttpRequest): Boolean = self(req) && other(req)
  }

  def ||(other: RouteMatcher): RouteMatcher = new RouteMatcher {
    override def apply(req: HttpRequest): Boolean = self(req) || other(req)
  }

  def unary_! : RouteMatcher = new RouteMatcher {
    override def apply(req: HttpRequest): Boolean = !self(req)
  }
}

/**
  * TODO: Add description.
  *
  * @author Vladimir Konstantinov
  */
object RouteMatcher {

  def apply(test: HttpRequest => Boolean): RouteMatcher = new RouteMatcher {
    override def apply(req: HttpRequest): Boolean = test(req)
  }

  def MethodIs(method: String) = apply(_.method equalsIgnoreCase method)
  def PathIs(path: String) = apply(_.path == path)
  def PathStartsWith(prefix: String) = apply(_.path startsWith prefix)
  def HasHeaderValue(name: String, value: String) = apply(_.headers.all(name).exists(_ equalsIgnoreCase value))
}
