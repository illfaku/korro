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

package org.oxydev.korro.http.tools

import org.oxydev.korro.http.api.HttpRequest
import org.oxydev.korro.http.api.HttpResponse.Status.NotFound

import akka.actor.Actor

/**
 * Actor for handling `HttpRequest` messages.
 * It will return `HttpResponse.Status.NotFound()` to sender if request unhandled.
 *
 * <p>Note: this trait overrides `unhandled` method, so if you want to override it too do not forget to call
 * `super.unhandled`.
 *
 * @author Vladimir Konstantinov
 */
trait HttpActor extends Actor {

  override def unhandled(message: Any): Unit = message match {
    case req: HttpRequest => sender ! NotFound()
    case _ => super.unhandled(message)
  }
}
