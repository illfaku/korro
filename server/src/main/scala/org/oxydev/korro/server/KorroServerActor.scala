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
package org.oxydev.korro.server

import org.oxydev.korro.server.actor.HttpServerActor
import org.oxydev.korro.server.config.ServerConfig
import org.oxydev.korro.util.config.wrapped

import akka.actor.{Actor, Props}
import com.typesafe.config.Config

import java.util.Collections.emptySet

import scala.collection.JavaConversions._

/**
 * The main actor that starts all configured http servers as its child actors.
 *
 * @author Vladimir Konstantinov
 */
object KorroServerActor {
  def props(config: Config): Props = Props(new KorroServerActor(config))
}

private [server] class KorroServerActor(config: Config) extends Actor {

  config.findObject("korro.server").map(_.keySet).getOrElse(emptySet) foreach { name =>
    HttpServerActor.create(new ServerConfig(name, config.getConfig(s"korro.server.$name")))
  }

  override def receive = Actor.emptyBehavior
}
