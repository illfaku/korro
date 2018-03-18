/*
 * Copyright 2018 Vladimir Konstantinov
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

import com.github.illfaku.korro.dto.HttpRequest
import com.github.illfaku.korro.dto.HttpResponse.Status.{BadRequest, NotFound}

import akka.actor.Actor

/**
 * Mixin for Actor. Provides helper methods to use as decorators for `receive` method.
 * <br><br>
 * Actor in example below will return HTTP response with status 404 to sender for any unhandled HTTP request.
 * {{{
 *   class MyHttpActor extends Actor with HttpBehavior {
 *     override def receive = notFoundIfUnhandled {
 *       case Get(Path("/employees")) => ???
 *     }
 *   }
 * }}}
 */
trait HttpBehavior { this: Actor =>

  /**
   * Will return HTTP response with status 400 to sender for any unhandled HTTP request.
   * @param receive Your behavior.
   */
  def badRequestIfUnhandled(receive: Receive): Receive = receive orElse {
    case _: HttpRequest => sender ! BadRequest()
  }

  /**
   * Will return HTTP response with status 404 to sender for any unhandled HTTP request.
   * @param receive Your behavior.
   */
  def notFoundIfUnhandled(receive: Receive): Receive = receive orElse {
    case _: HttpRequest => sender ! NotFound()
  }
}
