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

import io.cafebabe.http.server.api.{HttpContent, HttpHeaders, HttpRequest, QueryParameters}
import io.netty.handler.codec.http.{FullHttpRequest, QueryStringDecoder}

import java.util.{Iterator => JIterator, List => JList, Map => JMap}

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 * @author Vladimir Konstantinov
 * @version 1.0 (4/14/2015)
 */
object NettyHttpRequest {

  def apply(request: FullHttpRequest): HttpRequest = {
    val uri = new QueryStringDecoder(request.getUri)
    HttpRequest(
      request.getMethod.name,
      uri.path,
      parameters(uri.parameters),
      headers(request.headers.iterator),
      new HttpContent(request.content)
    )
  }

  private def parameters(params: JMap[String, JList[String]]): QueryParameters = {
    new QueryParameters(params.toMap.mapValues(_.toList))
  }

  private def headers(it: JIterator[JMap.Entry[String, String]]): HttpHeaders = {
    val result = mutable.Map.empty[String, List[String]]
    while (it.hasNext) {
      val entry = it.next
      val key = entry.getKey
      val list = result.getOrElse(key, List.empty)
      result += key -> (entry.getValue :: list)
    }
    new HttpHeaders(Map.empty ++ result)
  }
}
