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
package org.oxydev.korro.http.actor

import org.oxydev.korro.http.client.actor.KorroHttpClientActor

import akka.actor.Props
import com.typesafe.config.Config

/**
 * The main actor that starts all configured http clients as its child actors.
 *
 * @author Vladimir Konstantinov
 */
object KorroHttpClient {
  def props(config: Config): Props = Props(new KorroHttpClientActor(config))
}
