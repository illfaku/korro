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
package io.cafebabe.korro.server

import io.cafebabe.korro.server.actor.KorroServerActor

import aQute.bnd.annotation.component.{Activate, Component, Deactivate, Reference}
import akka.actor.{ActorRef, ActorSystem}
import org.osgi.framework.BundleContext
import org.slf4j.LoggerFactory

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
@Component
class HttpServerComponent {

  private val log = LoggerFactory.getLogger(getClass)

  private var actors: ActorSystem = null

  private var korro: ActorRef = null

  @Activate def activate(ctx: BundleContext): Unit = {
    korro = KorroServerActor.create(actors)
  }

  @Deactivate def deactivate(ctx: BundleContext): Unit = {
    actors.stop(korro)
  }

  @Reference def setActorSystem(actorSystem: ActorSystem): Unit = { actors = actorSystem }
}
