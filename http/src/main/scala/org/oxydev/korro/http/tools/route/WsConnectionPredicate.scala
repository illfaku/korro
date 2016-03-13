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

import org.oxydev.korro.http.api.ws.WsConnection
import org.oxydev.korro.util.lang.{Predicate, Predicate1}

/**
 * Predicates for [[WsRouter]].
 */
object WsConnectionPredicate {

  def apply(test: WsConnection => Boolean): Predicate1[WsConnection] = Predicate(test)

  def HostIs(host: String) = apply(wc => wc.host == host || wc.host.split(':').head == host)
  def PathIs(path: String) = apply(_.path == path)
  def PathStartsWith(prefix: String) = apply(_.path startsWith prefix)
}
