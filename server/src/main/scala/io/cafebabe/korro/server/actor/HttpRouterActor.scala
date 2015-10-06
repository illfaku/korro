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

import akka.actor._
import io.cafebabe.korro.api.http.route._
import io.cafebabe.korro.util.config.wrapped

import scala.collection.mutable
import scala.concurrent.duration._

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object HttpRouterActor {

  val name = "http-router"
  val path = s"${KorroActor.path}/$name"

  def create(implicit factory: ActorRefFactory): ActorRef = factory.actorOf(Props(new HttpRouterActor), name)

  def selection(implicit factory: ActorRefFactory): ActorSelection = factory.actorSelection(path)
}

class HttpRouterActor extends Actor {

  private implicit val ordering = Ordering.Int.on[Route](_.path.length)

  private val routes = mutable.Map.empty[Int, List[Route]]

  override def preStart(): Unit = {
    val servers = context.system.settings.config.findConfigList("korro.servers")
    routes ++= servers.map(c => c.getInt("port") -> c).toMap mapValues { config =>
      val httpRequestTimeout = config.findFiniteDuration("HTTP.requestTimeout").getOrElse(60 seconds)
      val httpRoutes: List[Route] = config.findConfigList("HTTP.routes").toList.map { r =>
        HttpRoute(r.getString("path"), httpRequestTimeout, r.getString("actor"))
      }

      val maxFramePayloadLength = config.findBytes("WebSocket.maxFramePayloadLength").getOrElse(65536L).toInt
      val wsCompression = config.findBoolean("WebSocket.compression").getOrElse(false)
      val wsRoutes: List[Route] = config.findConfigList("WebSocket.routes").toList.map { r =>
        WsRoute(r.getString("path"), maxFramePayloadLength, wsCompression, r.getString("actor"))
      }

      httpRoutes ++ wsRoutes
    }
  }

  override def postStop(): Unit = routes.clear()

  override def receive = {

    case SetRoute(port, route) => routes += port -> (route :: routes.getOrElse(port, List.empty))

    case GetRoute(port, path) =>
      sender ! routes.get(port).map(_.filter(path startsWith _.path)).filter(_.nonEmpty).map(_.max)
  }
}
