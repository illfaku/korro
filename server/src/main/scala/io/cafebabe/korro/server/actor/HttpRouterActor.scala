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
package io.cafebabe.korro.server.actor

import io.cafebabe.korro.api.http.route._
import io.cafebabe.korro.util.config.wrapped

import akka.actor._
import com.typesafe.config.Config

import scala.collection.mutable

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object HttpRouterActor {

  val name = "router"
  def path(port: Int) = s"${HttpServerActor.path(port)}/$name"

  def create(config: Config)(implicit factory: ActorRefFactory): ActorRef = {
    factory.actorOf(Props(new HttpRouterActor(config)), name)
  }

  def selection(port: Int)(implicit factory: ActorRefFactory): ActorSelection = factory.actorSelection(path(port))
}

class HttpRouterActor(config: Config) extends Actor {

  private implicit val ordering = Ordering.Int.on[Route](_.path.length)

  private val routes = mutable.Set.empty[Route]

  override def preStart(): Unit = {
    routes ++= config.findConfigList("HTTP.routes")
      .filter(_.hasPath("path")).filter(_.hasPath("actor"))
      .map(r => HttpRoute(r.getString("path"), r.getString("actor")))

    routes ++= config.findConfigList("WebSocket.routes")
      .filter(_.hasPath("path")).filter(_.hasPath("actor"))
      .map(r => WsRoute(r.getString("path"), r.getString("actor")))
  }

  override def postStop(): Unit = routes.clear()

  override def receive = {

    case SetRoute(route) => routes += route
    case UnsetRoute(route) => routes -= route

    case path: String => sender ! Some(routes.filter(path startsWith _.path)).filter(_.nonEmpty).map(_.max)
  }
}
