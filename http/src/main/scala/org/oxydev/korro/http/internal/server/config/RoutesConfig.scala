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
package org.oxydev.korro.http.internal.server.config

import org.oxydev.korro.util.config.extended
import org.oxydev.korro.util.log.Logging

import com.typesafe.config.Config
import io.netty.handler.codec.http.{HttpHeaders, HttpMethod, HttpRequest}

import java.util.regex.Pattern

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object RoutesConfig extends Logging {
  def apply(configs: Iterable[Config]): RoutesConfig = {
    val routes = configs.filter(_.hasPath("actor")).map(new RouteConfig(_))
    new RoutesConfig(routes.toList)
  }
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class RoutesConfig(routes: List[RouteConfig]) {
  def apply(req: HttpRequest): Option[String] = {
    def find(tail: List[RouteConfig]): Option[String] = tail match {
      case x :: xs => if (x.test(req)) Some(x.actor) else find(xs)
      case Nil => None
    }
    find(routes)
  }
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
private class RouteConfig(config: Config) {

  val actor = config.getString("actor")


  private val method: Option[String] = config.findString("method")

  private val path: Option[String] = config.findString("path")

  private val pathPrefix: Option[String] = config.findString("path-prefix")

  private val pathPattern: Option[Pattern] = config.findString("path-pattern").map(Pattern.compile)

  private val host: Option[String] = config.findString("host")

  private val headers: Iterable[(String, String)] = config.findStringList("headers") map { header =>
    val a = header.split("=", 2).map(_.trim)
    if (a.length == 1) a(0) -> null else a(0) -> a(1)
  }


  def test(req: HttpRequest): Boolean = {
    testMethod(req.getMethod) &&
      testPath(req.getUri) &&
      testPathPrefix(req.getUri) &&
      testPathPattern(req.getUri) &&
      testHost(req.headers) &&
      testHeaders(req.headers)
  }


  private def testMethod(m: HttpMethod): Boolean = method.forall(_ equalsIgnoreCase m.name)

  private def testPath(uri: String): Boolean = path.forall(_ == path(uri))

  private def testPathPrefix(uri: String): Boolean = pathPrefix.forall(uri.startsWith)

  private def testPathPattern(uri: String): Boolean = pathPattern.forall(_.matcher(path(uri)).matches)

  private def testHost(h: HttpHeaders): Boolean = host forall { hst =>
    val hh = h.get(HttpHeaders.Names.HOST)
    hh != null && (hh == hst || hh.split(':').head == hst)
  }

  private def testHeaders(h: HttpHeaders): Boolean = headers forall {
    case (name, null) => h.contains(name)
    case (name, value) => h.contains(name, value, true)
  }


  private def path(uri: String): String = uri.split('?').head
}
