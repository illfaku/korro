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
package org.oxydev.korro.http.api.route

import org.oxydev.korro.http.api.HttpRequest

/**
 * Predicate to route [[org.oxydev.korro.http.api.HttpRequest HttpRequest]] and
 * [[org.oxydev.korro.http.api.ws.WsConnection WsConnection]] objects to your actors.
 */
sealed trait RoutePredicate {

  /**
   * Combines this and given predicates into `And` predicate.
   * @param p Predicate to combine with.
   * @return `And` predicate.
   */
  def &&(p: RoutePredicate): RoutePredicate = RoutePredicate.And(this, p)

  /**
   * Combines this and given predicates into `Or` predicate.
   * @param p Predicate to combine with.
   * @return `Or` predicate.
   */
  def ||(p: RoutePredicate): RoutePredicate = RoutePredicate.Or(this, p)
}

object RoutePredicate {

  /**
   * Two predicates combined using `OR` condition.
   */
  case class Or(a: RoutePredicate, b: RoutePredicate) extends RoutePredicate

  /**
   * Two predicates combined using `AND` condition.
   */
  case class And(a: RoutePredicate, b: RoutePredicate) extends RoutePredicate


  /**
   * Checks request method equality.
   */
  case class MethodIs(method: HttpRequest.Method) extends RoutePredicate

  /**
   * Checks request path equality.
   */
  case class PathIs(path: String) extends RoutePredicate

  /**
   * Checks that request path starts with given prefix.
   */
  case class PathStartsWith(prefix: String) extends RoutePredicate

  /**
   * Checks that request path ends with given prefix.
   */
  case class PathEndsWith(suffix: String) extends RoutePredicate

  /**
   * Checks that request path matches given regular expression.
   */
  case class PathMatch(regexp: String) extends RoutePredicate

  /**
   * Checks that request has query parameter with given name.
   */
  case class HasQueryParam(name: String) extends RoutePredicate

  /**
   * Checks that request has query parameter with given name and value.
   */
  case class HasQueryParamValue(name: String, value: String) extends RoutePredicate

  /**
   * Checks that request has header with given name.
   */
  case class HasHeader(name: String) extends RoutePredicate

  /**
   * Checks that request has header with given name and value.
   */
  case class HasHeaderValue(name: String, value: String) extends RoutePredicate
}
