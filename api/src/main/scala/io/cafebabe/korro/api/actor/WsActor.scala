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
package io.cafebabe.korro.api.actor

import io.cafebabe.korro.api.ws.WsMessage

import akka.actor.Actor

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
trait WsActor extends Actor {

  type WsReceive = PartialFunction[WsMessage, Unit]

  override final def receive: Receive = {
    case msg: WsMessage => receiveWs.applyOrElse(msg, unhandled)
  }

  def receiveWs: WsReceive
}
