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

import io.cafebabe.korro.api.http.QueryParams
import io.cafebabe.korro.server.util.MimeTypes.FormUrlEncoded

import io.netty.handler.codec.http.HttpConstants.DEFAULT_CHARSET
import io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE
import io.netty.handler.codec.http.{FullHttpRequest, QueryStringDecoder, QueryStringEncoder}

import scala.collection.JavaConversions._

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object QueryParamsConverter {

  def fromNetty(request: FullHttpRequest): QueryParams = new QueryParams(fromUri(request) ++ fromBody(request))

  private def fromUri(request: FullHttpRequest): Map[String, List[String]] = {
    val decoder = new QueryStringDecoder(request.getUri)
    decoder.parameters.toMap.mapValues(_.toList)
  }

  private def fromBody(request: FullHttpRequest): Map[String, List[String]] = {
    if (FormUrlEncoded == request.headers.get(CONTENT_TYPE)) {
      val params = request.content.toString(DEFAULT_CHARSET)
      val decoder = new QueryStringDecoder(params, false)
      decoder.parameters.toMap.mapValues(_.toList)
    } else Map.empty
  }

  def toNetty(parameters: QueryParams): String = {
    val encoder = new QueryStringEncoder("") // without path
    parameters.toMap foreach { case (name, values) =>
      values foreach { value =>
        encoder.addParam(name, value)
      }
    }
    encoder.toString.substring(1) // removing of '?'
  }
}
