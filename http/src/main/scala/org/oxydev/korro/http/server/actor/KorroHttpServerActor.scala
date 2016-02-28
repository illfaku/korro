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
package org.oxydev.korro.http.server.actor

import org.oxydev.korro.http.server.config.ServerConfig
import org.oxydev.korro.util.config.wrapped

import akka.actor.Actor
import com.typesafe.config.Config

import java.util.Collections.emptySet

import scala.collection.JavaConversions._

/**
 * The main actor that starts all configured http servers as its child actors.
 *
 * @author Vladimir Konstantinov
 */
class KorroHttpServerActor(config: Config) extends Actor {

  config.findObject("korro.server").map(_.keySet).getOrElse(emptySet) foreach { name =>
    HttpServerActor.create(new ServerConfig(name, config.getConfig(s"korro.server.$name")))
  }

  override def receive = Actor.emptyBehavior
}
