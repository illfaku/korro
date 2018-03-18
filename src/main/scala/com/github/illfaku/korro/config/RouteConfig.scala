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
package com.github.illfaku.korro.config

import com.github.illfaku.korro.util.configOptions

import com.typesafe.config.Config

/**
 * Configuration for router to set actor at specified actor path as handler of matched requests.<br>
 * It can be sent to server actor to add new route.<br>
 * To remove all routes of an actor from router your should send `RouteConfig` with needed actor path and
 * `predicate = RequestPredicate.False`.
 *
 * @param actorPath Actor path.
 * @param predicate Predicate to test requests against.
 * @param instructions Set of instructions for request handling.
 */
case class RouteConfig(
  actorPath: String,
  predicate: RequestPredicate = RequestPredicate.True,
  instructions: List[HttpInstruction] = Nil
)

object RouteConfig {

  def extract(config: Config): RouteConfig = {
    RouteConfig(
      config.getString("actor"),
      config.findConfig("predicate").map(RequestPredicate.extract).getOrElse(RequestPredicate.True),
      config.findConfig("instructions").map(HttpInstruction.extract).getOrElse(Nil)
    )
  }
}
