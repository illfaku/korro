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

import io.cafebabe.korro.util.config.wrapped

import aQute.bnd.annotation.component.{Activate, Component, Deactivate, Reference}
import akka.actor.ActorSystem
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

  private var servers = List.empty[HttpServer]

  @Activate def activate(ctx: BundleContext): Unit = {
    actors.settings.config.findConfigList("cafebabe.http.servers") foreach { config =>
      val port = config.findString("port").getOrElse("UNSPECIFIED")
      try {
        servers = HttpServer(config, actors) :: servers
        log.info("Started HTTP server on port {}.", port)
      } catch {
        case e: Throwable => log.error(s"Failed to start HTTP server on port $port.", e)
      }
    }
  }

  @Deactivate def deactivate(ctx: BundleContext): Unit = {
    servers foreach { server =>
      try {
        server.stop()
        log.info("Stopped HTTP server on port {}.", server.port)
      } catch {
        case e: Throwable => log.error(s"Failed to stop HTTP server on port ${server.port}.", e)
      }
    }
  }

  @Reference def setActorSystem(actorSystem: ActorSystem): Unit = { actors = actorSystem }
}
