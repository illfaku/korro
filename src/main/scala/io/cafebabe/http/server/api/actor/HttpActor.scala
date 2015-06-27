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
package io.cafebabe.http.server.api.actor

import akka.actor.{Actor, Status}
import io.cafebabe.http.server.api.HttpRequest
import io.cafebabe.http.server.api.exception.NotFoundException

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
abstract class HttpActor extends Actor {

  type HttpReceive = PartialFunction[HttpRequest, Unit]

  override final def receive = {
    case req: HttpRequest => receiveHttp.applyOrElse(req, notFound)
  }

  def receiveHttp: HttpReceive

  private def notFound(req: HttpRequest): Unit = sender ! Status.Failure(new NotFoundException)
}