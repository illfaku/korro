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
package com.github.illfaku.korro.tools

import org.oxydev.korro.api.HttpResponse.Status.NotFound
import akka.actor.Actor
import com.github.illfaku.korro.api.HttpRequest

/**
 * Actor for handling [[HttpRequest]] messages.
 * It will return response with status 404 to sender if request is unhandled.
 *
 * <p>Note: this trait overrides `unhandled` method, so if you want to override it too do not forget to call
 * `super.unhandled`.
 */
trait HttpActor extends Actor {

  override def unhandled(message: Any): Unit = message match {
    case req: HttpRequest => sender ! NotFound()
    case _ => super.unhandled(message)
  }
}
