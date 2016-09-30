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
package org.oxydev.korro.http.api.config

import org.oxydev.korro.http.api.HttpRequest
import org.oxydev.korro.http.api.route.{RouteInstruction, RoutePredicate}
import org.oxydev.korro.util.config.extended

import com.typesafe.config.Config

import scala.concurrent.duration._

case class ServerConfig(
  name: String,
  port: Int = ServerConfig.Defaults.port,
  workerGroupSize: Int = ServerConfig.Defaults.workerGroupSize,
  logger: String = ServerConfig.Defaults.logger,
  instructions: List[RouteInstruction] = Nil,
  routes: List[RouteConfig] = Nil
)

object ServerConfig {

  object Defaults {
    val port: Int = 8080
    val workerGroupSize: Int = 1
    val logger: String = "korro-server"
    val requestTimeout: FiniteDuration = 5 seconds
  }

  def extract(name: String, config: Config): ServerConfig = {
    ServerConfig(
      name,
      config.findInt("port").getOrElse(Defaults.port),
      config.findInt("worker-group-size").getOrElse(Defaults.workerGroupSize),
      config.findString("logger").getOrElse(Defaults.logger),
      config.findConfig("instructions").map(extractInstructions).getOrElse(Nil),
      config.findConfigList("routes").filter(_.hasPath("actor")).map(extractRoute)
    )
  }

  private def extractInstructions(config: Config): List[RouteInstruction] = {
    List(
      config.findFiniteDuration("request-timeout").map(RouteInstruction.RequestTimeout),
      config.findBytes("max-content-length").map(RouteInstruction.MaxContentLength),
      config.findBoolean("content-as-file").map(RouteInstruction.ContentAsFile),
      config.findDuration("file-content-remove-delay").map(RouteInstruction.FileContentRemoveDelay),
      config.findInt("response-compression").map(RouteInstruction.ResponseCompressionLevel),
      config.findString("ws-logger").map(RouteInstruction.WsLogger),
      config.findInt("max-ws-frame-payload-length").map(RouteInstruction.MaxWsFramePayloadLength)
    ).flatten
  }

  private def extractRoute(config: Config): RouteConfig = {
    RouteConfig(
      config.getString("actor"),
      config.findConfig("predicate").map(extractPredicate(_, _ && _)).getOrElse(RoutePredicate.True),
      config.findConfig("instructions").map(extractInstructions).getOrElse(Nil)
    )
  }

  private def extractPredicate(config: Config, op: (RoutePredicate, RoutePredicate) => RoutePredicate): RoutePredicate = {
    val predicates = List[Iterable[RoutePredicate]](
      config.findString("method-is").map(HttpRequest.Method(_)).map(RoutePredicate.MethodIs),
      config.findString("path-is").map(RoutePredicate.PathIs),
      config.findString("path-starts-with").map(RoutePredicate.PathStartsWith),
      config.findString("path-ends-with").map(RoutePredicate.PathEndsWith),
      config.findString("path-match").map(RoutePredicate.PathMatch),
      config.findString("has-query-param").map(extractParamPredicate(_, "=")),
      config.findStringList("has-query-params").map(extractParamPredicate(_, "=")),
      config.findString("has-header").map(extractParamPredicate(_, ":")),
      config.findStringList("has-headers").map(extractParamPredicate(_, ":")),
      config.findConfig("or").map(extractPredicate(_, _ || _)),
      config.findConfig("and").map(extractPredicate(_, _ && _))
    )
    predicates.flatten.reduceOption(op) getOrElse RoutePredicate.True
  }

  private def extractParamPredicate(config: String, separator: String): RoutePredicate = {
    val parts = config.split(separator, 2)
    val name = parts.head.trim
    parts.drop(1).headOption.map(_.trim)
      .map(RoutePredicate.HasQueryParamValue(name, _))
      .getOrElse(RoutePredicate.HasQueryParam(name))
  }
}
