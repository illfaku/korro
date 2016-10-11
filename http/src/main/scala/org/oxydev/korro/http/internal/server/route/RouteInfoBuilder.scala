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
package org.oxydev.korro.http.internal.server.route

import org.oxydev.korro.http.api.config.RouteConfig
import org.oxydev.korro.http.api.route.RouteInstruction._
import org.oxydev.korro.http.api.route.{RouteInstruction, RoutePredicate}

import akka.actor.ActorRef

class RouteInfoBuilder(fallback: MergedRouteInstructions) {

  import RouteInfoBuilder.merge

  def prepare(ref: ActorRef, predicate: RoutePredicate, instructions: List[RouteInstruction]): RouteInfo = {
    RouteInfo(ActorRefRoute(ref), predicate, merge(instructions, fallback))
  }

  def prepare(route: RouteConfig): RouteInfo = {
    RouteInfo(ActorPathRoute(route.actorPath), route.predicate, merge(route.instructions, fallback))
  }
}

object RouteInfoBuilder {

  def apply(instructions: List[RouteInstruction]): RouteInfoBuilder = {
    new RouteInfoBuilder(merge(instructions, MergedRouteInstructions.default))
  }

  private def merge(instructions: List[RouteInstruction], result: MergedRouteInstructions): MergedRouteInstructions = {
    instructions match {
      case RequestTimeout(duration) :: tail => merge(tail, result.copy(requestTimeout = duration))
      case MaxContentLength(length) :: tail => merge(tail, result.copy(maxContentLength = length))
      case ContentAsFile(enabled) :: tail => merge(tail, result.copy(contentAsFile = enabled))
      case FileContentRemoveDelay(delay) :: tail => merge(tail, result.copy(fileContentRemoveDelay = delay))
      case ResponseCompression(enabled) :: tail => merge(tail, result.copy(responseCompression = enabled))
      case ResponseCompressionLevel(level) :: tail => merge(tail, result.copy(responseCompressionLevel =level))
      case MaxWsFramePayloadLength(length) :: tail => merge(tail, result.copy(maxWsFramePayloadLength = length))
      case WsLogger(name) :: tail => merge(tail, result.copy(wsLogger = name))
      case SimpleWsLogging(enabled) :: tail => merge(tail, result.copy(simpleWsLogging = enabled))
      case Nil => result
    }
  }
}
