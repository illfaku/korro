/*
 * Copyright (C) 2015  Vladimir Konstantinov, Yuriy Gintsyak
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
package io.cafebabe.korro.api.actor

import io.cafebabe.korro.api.http.HttpRequest
import io.cafebabe.korro.api.http.HttpStatus.NotFound

import akka.actor.{Actor, ActorRef}

import scala.collection.mutable

/**
 * TODO: Add description.
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
      case None => sender ! NotFound()
    }
    case _ => super.unhandled(message)
  }
}

object HttpRouter {
  case class SetRoute(actor: ActorRef, matcher: RouteMatcher)
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

object RouteMatcher {

  def apply(test: HttpRequest => Boolean): RouteMatcher = new RouteMatcher {
    override def apply(req: HttpRequest): Boolean = test(req)
  }

  def MethodIs(method: String) = apply(_.method equalsIgnoreCase method)
  def PathIs(path: String) = apply(_.path == path)
  def PathStartsWith(prefix: String) = apply(_.path startsWith prefix)
  def HasHeaderValue(name: String, value: String) = apply(_.headers.all(name).exists(_ == value))
}
