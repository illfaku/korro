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

import org.oxydev.korro.http.api.ws.{SetTarget, WsConnection}
import org.oxydev.korro.util.lang.Predicate1

import akka.actor.{Actor, ActorRef}

import scala.collection.mutable

/**
 * Actor for handling [[WsConnection]] messages with routing functionality.
 * Accepts [[WsRouter#SetRoute]] and [[WsRouter#UnsetRoute]] commands.
 *
 * <p>If this actor will not process [[WsConnection]] itself in `receive` method then it will try to find actor
 * that matches this message and forward message to it, otherwise it will send [[SetTarget]]`(None)` to sender.
 *
 * <p>Note: this trait overrides `unhandled` method, so if you want to override it too do not forget to call
 * `super.unhandled`.
 */
trait WsRouter extends Actor {

  private val routes = mutable.Map.empty[ActorRef, Predicate1[WsConnection]]

  /**
   * Forwards [[WsConnection]] to matching route if found, otherwise sends [[SetTarget]]`(None)` to sender.
   *
   * <p> Note: because of usage of `sender` method inside be sure to use this method only within this actor.
   *
   * @param wc Connection to route.
   */
  protected def route(wc: WsConnection): Unit = {
    routes.find(_._2(wc)) match {
      case Some((actor, _)) => actor forward wc
      case None => sender ! SetTarget(None)
    }
  }

  override def unhandled(message: Any): Unit = message match {
    case WsRouter.SetRoute(ref, predicate) => routes += (ref -> predicate)
    case WsRouter.UnsetRoute(ref) => routes -= ref
    case wc: WsConnection => route(wc)
    case _ => super.unhandled(message)
  }
}

/**
 * WsRouter commands.
 */
object WsRouter {

  /**
   * Command for WsRouter to set your actor as handler of matched connections.
   *
   * @param ref Actor reference to set.
   * @param predicate Predicate to test connections against.
   */
  case class SetRoute(ref: ActorRef, predicate: Predicate1[WsConnection])

  /**
   * Command for WsRouter to remove your actor from handlers list.
   *
   * @param ref Actor reference to unset.
   */
  case class UnsetRoute(ref: ActorRef)
}
