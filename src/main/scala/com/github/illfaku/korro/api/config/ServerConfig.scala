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
package com.github.illfaku.korro.api.config

import com.github.illfaku.korro.api.HttpRequest
import org.oxydev.korro.api.route.{RouteInstruction, RoutePredicate}
import com.github.illfaku.korro.util.config.extended
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
      config.findBoolean("response-compression").map(RouteInstruction.ResponseCompression),
      config.findInt("response-compression-level").map(RouteInstruction.ResponseCompressionLevel),
      config.findInt("max-ws-frame-payload-length").map(RouteInstruction.MaxWsFramePayloadLength),
      config.findString("ws-logger").map(RouteInstruction.WsLogger),
      config.findBoolean("simple-ws-logging").map(RouteInstruction.SimpleWsLogging)
    ).flatten
  }

  private def extractRoute(config: Config): RouteConfig = {
    RouteConfig(
      config.getString("actor"),
      config.findConfig("predicate").map(extractPredicate(_, andReduce)).getOrElse(RoutePredicate.True),
      config.findConfig("instructions").map(extractInstructions).getOrElse(Nil)
    )
  }

  private def extractPredicate(config: Config, op: (RoutePredicate, RoutePredicate) => RoutePredicate): RoutePredicate = {
    val predicates = List(
      config.findString("method-is").map(HttpRequest.Method(_)).map(RoutePredicate.MethodIs),
      config.findString("path-is").map(RoutePredicate.PathIs),
      config.findString("path-starts-with").map(RoutePredicate.PathStartsWith),
      config.findString("path-ends-with").map(RoutePredicate.PathEndsWith),
      config.findString("path-match").map(RoutePredicate.PathMatch),
      config.findString("has-query-param").map(extractQueryPredicate),
      config.findStringList("has-any-query-param").map(extractQueryPredicate).reduceOption(orReduce),
      config.findStringList("has-all-query-params").map(extractQueryPredicate).reduceOption(andReduce),
      config.findString("has-header").map(extractHeaderPredicate),
      config.findStringList("has-any-header").map(extractHeaderPredicate).reduceOption(orReduce),
      config.findStringList("has-all-headers").map(extractHeaderPredicate).reduceOption(andReduce),
      config.findBoolean("is-ws-handshake").map(RoutePredicate.IsWsHandshake),
      config.findConfig("or").map(extractPredicate(_, orReduce)),
      config.findConfig("and").map(extractPredicate(_, andReduce))
    )
    predicates.flatten.reduceOption(op) getOrElse RoutePredicate.True
  }

  private def extractQueryPredicate(config: String): RoutePredicate = {
    val parts = config.split("=", 2)
    val name = parts.head.trim
    parts.drop(1).headOption.map(_.trim)
      .map(RoutePredicate.HasQueryParamValue(name, _))
      .getOrElse(RoutePredicate.HasQueryParam(name))
  }

  private def extractHeaderPredicate(config: String): RoutePredicate = {
    val parts = config.split(":", 2)
    val name = parts.head.trim
    parts.drop(1).headOption.map(_.trim)
      .map(RoutePredicate.HasHeaderValue(name, _))
      .getOrElse(RoutePredicate.HasHeader(name))
  }

  private val orReduce: (RoutePredicate, RoutePredicate) => RoutePredicate = _ || _

  private val andReduce: (RoutePredicate, RoutePredicate) => RoutePredicate = _ && _
}
