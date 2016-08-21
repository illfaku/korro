/*
 * Copyright 2016 Vladimir Konstantinov, Yuriy Gintsyak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.oxydev.korro.http.internal.server.actor

import org.oxydev.korro.http.internal.server.config.ServerConfig
import org.oxydev.korro.util.config.extended

import akka.actor.Actor
import com.typesafe.config.Config

import java.util.Collections.emptySet

import scala.collection.JavaConversions._

/**
 * The main actor that starts all configured http servers as its child actors.
 */
class KorroHttpServerActor(config: Config) extends Actor {

  config.findObject("korro.server").map(_.keySet).getOrElse(emptySet) foreach { name =>
    val serverConfig = new ServerConfig(name, config.getConfig(s"korro.server.$name"))
    HttpServerActor.create(name) ! serverConfig
  }

  override def receive = Actor.emptyBehavior
}
