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
import io.cafebabe.korro.util.config.wrapped

import akka.actor.{Actor, ActorRef, ActorRefFactory, Props}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object KorroClientActor {

  val name = "korro-client"
  val path = s"/user/$name"

  def create(implicit factory: ActorRefFactory): ActorRef = factory.actorOf(Props(new KorroClientActor), name)
}

class KorroClientActor extends Actor {

  override def preStart(): Unit = {
    context.system.settings.config.findConfigList("korro.clients").foreach(HttpClientActor.create)
  }

  override def receive = {
    case _ => ()
  }
}
