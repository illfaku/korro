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

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object HttpRouterActor {

  def name(port: Int) = s"router-$port"
  def path(port: Int) = s"${KorroActor.path}/${name(port)}"

  def create(config: Config)(implicit factory: ActorRefFactory): ActorRef = {
    factory.actorOf(Props(new HttpRouterActor(config)), name(config.getInt("port")))
  }

  def selection(port: Int)(implicit factory: ActorRefFactory): ActorSelection = factory.actorSelection(path(port))
}

class HttpRouterActor(config: Config) extends Actor {

  private implicit val ordering = Ordering.Int.on[Route](_.path.length)

  private var routes = List.empty[Route]

  override def preStart(): Unit = {
    val httpRoutes: List[Route] = config.findConfigList("HTTP.routes").toList.map { r =>
      HttpRoute(r.getString("path"), r.getString("actor"))
    }
    val wsRoutes: List[Route] = config.findConfigList("WebSocket.routes").toList.map { r =>
      WsRoute(r.getString("path"), r.getString("actor"))
    }
    routes = httpRoutes ++ wsRoutes
  }

  override def postStop(): Unit = routes = List.empty

  override def receive = {
    case SetRoute(port, route) => routes = route :: routes
    case path: String => sender ! Some(routes.filter(path startsWith _.path)).filter(_.nonEmpty).map(_.max)
  }
}
