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
   * Returns optional matching route.
   *
   * @param wc Connection to match.
   */
  protected def findRoute(wc: WsConnection): Option[ActorRef] = routes.find(_._2(wc)).map(_._1)

  /**
   * Forwards [[WsConnection]] to matching route if found, otherwise sends [[SetTarget]]`(None)` to sender.
   *
   * <p> Note: because of usage of `sender` method inside be sure to use this method only within this actor.
   *
   * @param wc Connection to route.
   */
  protected def route(wc: WsConnection): Unit = findRoute(wc) match {
    case Some(actor) => actor forward wc
    case None => sender ! SetTarget(None)
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
