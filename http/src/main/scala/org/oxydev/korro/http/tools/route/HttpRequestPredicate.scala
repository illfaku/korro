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
package org.oxydev.korro.http.tools.route

import org.oxydev.korro.http.api.HttpRequest
import org.oxydev.korro.util.lang.{Predicate, Predicate1}

/**
 * Predicates for [[HttpRouter]].
 */
object HttpRequestPredicate {

  def apply(test: HttpRequest => Boolean): Predicate1[HttpRequest] = Predicate(test)

  def MethodIs(method: String) = apply(_.method.name == method)
  def MethodIs(method: HttpRequest.Method) = apply(_.method == method)
  def PathIs(path: String) = apply(_.path == path)
  def PathStartsWith(prefix: String) = apply(_.path startsWith prefix)
  def HasHeader(name: String) = apply(_.headers.get(name).isDefined)
  def HasHeaderValue(name: String, value: String) = apply(_.headers.all(name).exists(_ equalsIgnoreCase value))
}
