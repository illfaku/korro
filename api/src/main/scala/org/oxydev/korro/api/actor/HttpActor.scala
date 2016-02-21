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
package org.oxydev.korro.api.actor

import org.oxydev.korro.api.http.{HttpRequest, HttpStatus}

import akka.actor.Actor

/**
 * Actor for handling `HttpRequest` messages.
 * It will return `HttpStatus.NotFound()` to sender if request unhandled.
 *
 * <p>Note: this trait overrides `unhandled` method, so if you want to override it too do not forget to call
 * `super.unhandled`.
 *
 * @author Vladimir Konstantinov
 */
trait HttpActor extends Actor {

  @deprecated("will be removed with receiveHttp method", "0.2.5")
  type HttpReceive = PartialFunction[HttpRequest, Unit]

  override def receive = {
    case req: HttpRequest => receiveHttp.applyOrElse(req, unhandled)
  }

  @deprecated("this is not really convenient, just override 'receive' as usual", "0.2.5")
  def receiveHttp: HttpReceive = {
    case msg => unhandled(msg)
  }

  override def unhandled(message: Any): Unit = message match {
    case req: HttpRequest => sender ! HttpStatus.NotFound()
    case _ => super.unhandled(message)
  }
}
