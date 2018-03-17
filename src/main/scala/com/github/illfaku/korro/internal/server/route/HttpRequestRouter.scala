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
package com.github.illfaku.korro.internal.server.route

import org.oxydev.korro.api.config.ServerConfig
import org.oxydev.korro.api.route.{RouteInstruction, RoutePredicate}
import com.github.illfaku.korro.util.net.QueryStringCodec
import akka.actor.ActorRef
import com.github.illfaku.korro.api.HttpParams
import io.netty.handler.codec.http.{HttpHeaderNames, HttpHeaderValues, HttpRequest}

class HttpRequestRouter(config: ServerConfig) {

  private val infoBuilder = RouteInfoBuilder(config.instructions)

  @volatile
  private var routes = config.routes.map(infoBuilder.prepare)

  def set(ref: ActorRef, predicate: RoutePredicate, instructions: List[RouteInstruction]): Unit = {
    routes = infoBuilder.prepare(ref, predicate, instructions) :: routes
  }

  def unset(ref: ActorRef): Unit = routes = routes.filterNot(_.dst == ref)

  /**
   * Returns optional matching route.
   *
   * @param req Request to match.
   */
  def find(req: HttpRequest): Option[RouteInfo] = {
    val (path: String, queryString: String) = {
      val pos = req.uri.indexOf('?')
      if (pos == -1) (req.uri, "") else (req.uri.substring(0, pos), req.uri.substring(pos + 1))
    }
    val params = new HttpParams(QueryStringCodec.decode(queryString))
    routes.reverse.find(r => check(req, path, params, r.predicate))
  }

  private def check(req: HttpRequest, reqPath: String, reqParams: HttpParams, predicate: RoutePredicate): Boolean = {
    predicate match {
      case RoutePredicate.True => true
      case RoutePredicate.MethodIs(method) => req.method.name == method.name
      case RoutePredicate.PathIs(path) => reqPath == path
      case RoutePredicate.PathStartsWith(prefix) => reqPath startsWith prefix
      case RoutePredicate.PathEndsWith(suffix) => reqPath endsWith suffix
      case RoutePredicate.PathMatch(regexp) => regexp.r.findFirstIn(reqPath).isDefined
      case RoutePredicate.HasQueryParam(name) => reqParams.contains(name)
      case RoutePredicate.HasQueryParamValue(name, value) => reqParams.contains(name, value)
      case RoutePredicate.HasHeader(name) => req.headers.contains(name)
      case RoutePredicate.HasHeaderValue(name, value) => req.headers.contains(name, value, false)
      case RoutePredicate.IsWsHandshake(value) => value ==
        req.headers.contains(HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE, true) &&
        req.headers.contains(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET, true)
      case RoutePredicate.Or(a, b) => check(req, reqPath, reqParams, a) || check(req, reqPath, reqParams, b)
      case RoutePredicate.And(a, b) => check(req, reqPath, reqParams, a) && check(req, reqPath, reqParams, b)
    }
  }
}
