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
package io.cafebabe.korro.server.convert

import io.cafebabe.korro.api.http.HttpRequest

import io.netty.handler.codec.http._

/**
 * Methods to convert HttpRequest from/to Netty's FullHttpRequest.
 *
 * @author Vladimir Konstantinov
 */
object HttpRequestConverter {

  def fromNetty(request: FullHttpRequest, pathPrefix: String): Either[ConversionFailure, HttpRequest] = {
    for {
      content <- HttpContentConverter.fromNetty(request.content, request.headers).right
    } yield HttpRequest(
      request.getMethod.name,
      new QueryStringDecoder(request.getUri).path.substring(pathPrefix.length),
      QueryParamsConverter.fromNetty(request),
      content,
      HttpHeadersConverter.fromNetty(request.headers)
    )
  }

  def toNetty(request: HttpRequest): FullHttpRequest = {

    val method = HttpMethod.valueOf(request.method)
    val uri = s"${request.path}?${QueryParamsConverter.toNetty(request.parameters)}"
    val (content, contentHeaders) = HttpContentConverter.toNetty(request.content)
    val headers = HttpHeadersConverter.toNetty(request.headers)

    val result = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, uri, content)
    result.headers.add(headers).add(contentHeaders)

    result
  }
}
