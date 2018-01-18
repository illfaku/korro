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
package io.cafebabe.korro.client

import io.cafebabe.korro.client.actor.HttpClientActor
import io.cafebabe.korro.util.akka.NoReceiveActor
import io.cafebabe.korro.util.config.wrapped

import akka.actor.{Actor, Props}

import java.util.Collections.emptySet

import scala.collection.JavaConverters._

object KorroClientActor {
  val props: Props = Props(new KorroClientActor)
}

private [client] class KorroClientActor extends Actor with NoReceiveActor {

  private val config = context.system.settings.config

  config.findObject("korro.client").map(_.keySet).getOrElse(emptySet).asScala foreach { name =>
    context.actorOf(HttpClientActor.props(name, config.getConfig(s"korro.client.$name")), name)
  }
}
