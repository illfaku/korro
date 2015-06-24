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
package io.cafebabe.http.server.impl

import io.cafebabe.http.server.api.{HttpContent, HttpHeaders, HttpRequest, QueryParams}
import io.netty.handler.codec.http.{FullHttpRequest, QueryStringDecoder}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 * @version 1.0 (4/14/2015)
 */
object NettyHttpRequest {
  def apply(request: FullHttpRequest, path: String): HttpRequest = {
    val uri = new QueryStringDecoder(request.getUri)
    HttpRequest(
      request.getMethod.name,
      uri.path.substring(path.length),
      QueryParams(uri.parameters),
      HttpHeaders(request.headers.iterator),
      HttpContent(request)
    )
  }
}
