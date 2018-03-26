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
 * Predicate to route [[com.github.illfaku.korro.dto.HttpRequest HttpRequest]] objects to your actors.
 */
sealed trait RequestPredicate {

  /**
   * Combines this and given predicates into `And` predicate.
   * @param p Predicate to combine with.
   * @return `And` predicate.
   */
  def &&(p: RequestPredicate): RequestPredicate = RequestPredicate.And(this, p)

  /**
   * Combines this and given predicates into `Or` predicate.
   * @param p Predicate to combine with.
   * @return `Or` predicate.
   */
  def ||(p: RequestPredicate): RequestPredicate = RequestPredicate.Or(this, p)
}

object RequestPredicate {

  /**
   * Two predicates combined using `OR` condition.
   */
  case class Or(a: RequestPredicate, b: RequestPredicate) extends RequestPredicate

  /**
   * Two predicates combined using `AND` condition.
   */
  case class And(a: RequestPredicate, b: RequestPredicate) extends RequestPredicate


  /**
   * Will pass all checks.
   */
  case object True extends RequestPredicate

  /**
   * Will fail all checks.
   */
  case object False extends RequestPredicate


  /**
   * Checks request method equality.
   */
  case class MethodIs(method: String) extends RequestPredicate

  /**
   * Checks request path equality.
   */
  case class PathIs(path: String) extends RequestPredicate

  /**
   * Checks that request path starts with given prefix.
   */
  case class PathStartsWith(prefix: String) extends RequestPredicate

  /**
   * Checks that request path ends with given prefix.
   */
  case class PathEndsWith(suffix: String) extends RequestPredicate

  /**
   * Checks that request path matches given regular expression.
   */
  case class PathMatch(regexp: String) extends RequestPredicate

  /**
   * Checks that request has query parameter with given name.
   */
  case class HasQueryParam(name: String) extends RequestPredicate

  /**
   * Checks that request has query parameter with given name and value.
   */
  case class HasQueryParamValue(name: String, value: String) extends RequestPredicate

  /**
   * Checks that request has header with given name.
   */
  case class HasHeader(name: String) extends RequestPredicate

  /**
   * Checks that request has header with given name and value.
   */
  case class HasHeaderValue(name: String, value: String) extends RequestPredicate


  def extract(config: Config): RequestPredicate = {
    List(
      config.findString("method-is").map(MethodIs),
      config.findString("path-is").map(PathIs),
      config.findString("path-starts-with").map(PathStartsWith),
      config.findString("path-ends-with").map(PathEndsWith),
      config.findString("path-match").map(PathMatch),
      config.findString("has-query-param").map(extractQueryPredicate),
      config.findStringList("has-any-query-param").map(extractQueryPredicate).reduceOption(orReduce),
      config.findStringList("has-all-query-params").map(extractQueryPredicate).reduceOption(andReduce),
      config.findString("has-header").map(extractHeaderPredicate),
      config.findStringList("has-any-header").map(extractHeaderPredicate).reduceOption(orReduce),
      config.findStringList("has-all-headers").map(extractHeaderPredicate).reduceOption(andReduce)
    ).flatten.reduceOption(andReduce) getOrElse True
  }

  private def extractQueryPredicate(config: String): RequestPredicate = {
    val parts = config.split("=", 2)
    val name = parts.head.trim
    parts.drop(1).headOption.map(_.trim)
      .map(HasQueryParamValue(name, _))
      .getOrElse(HasQueryParam(name))
  }

  private def extractHeaderPredicate(config: String): RequestPredicate = {
    val parts = config.split(":", 2)
    val name = parts.head.trim
    parts.drop(1).headOption.map(_.trim)
      .map(HasHeaderValue(name, _))
      .getOrElse(HasHeader(name))
  }

  private val orReduce: (RequestPredicate, RequestPredicate) => RequestPredicate = _ || _

  private val andReduce: (RequestPredicate, RequestPredicate) => RequestPredicate = _ && _


  def parse(predicate: String): RequestPredicate = {
    val parts = predicate.trim.split("\\s+")
    if (parts.length >= 3) {
      parts(0).split("\\|").map(MethodIs).reduce(orReduce) && parsePathPredicate(parts(1), parts(2))
    } else {
      False
    }
  }

  private def parsePathPredicate(kind: String, path: String): RequestPredicate = kind match {
    case "!" => PathIs(path)
    case "^" => PathStartsWith(path)
    case "$" => PathEndsWith(path)
    case "*" => PathMatch(path)
    case "#" => PathMatch(path.replace("#", "[^/]+"))
    case _ => False
  }
}
