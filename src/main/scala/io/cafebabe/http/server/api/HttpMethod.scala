/*
 * Copyright (C) 2015  Vladimir Konstantinov, Yuriy Gintsyak
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.cafebabe.http.server.api

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 * @version 1.0 (4/17/2015)
 */
sealed abstract class HttpMethod(name: String) {
  def unapply(req: HttpRequest): Option[(String, HttpRequest)] =
    if (req.method.equalsIgnoreCase(name)) Some(req.path, req) else None
}

object GET extends HttpMethod("GET")
object POST extends HttpMethod("POST")
object PUT extends HttpMethod("PUT")
object DELETE extends HttpMethod("DELETE")
object HEAD extends HttpMethod("HEAD")
object CONNECT extends HttpMethod("CONNECT")
object OPTIONS extends HttpMethod("OPTIONS")
object TRACE extends HttpMethod("TRACE")
