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
package io.cafebabe.http.server.impl.convert

import io.cafebabe.http.server.api.HttpHeaders

import io.netty.handler.codec.http.{DefaultHttpHeaders, HttpHeaders => NettyHttpHeaders}

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object HttpHeadersConverter {

  def fromNetty(headers: NettyHttpHeaders): HttpHeaders = {
    val result = mutable.Map.empty[String, List[String]]
    for (header <- headers) {
      val key = header.getKey
      val list = result.getOrElse(key, List.empty)
      result += key -> (header.getValue :: list)
    }
    new HttpHeaders(Map.empty ++ result)
  }

  def toNetty(headers: HttpHeaders): NettyHttpHeaders = {
    val result = new DefaultHttpHeaders
    headers.toMap foreach { case (name, values) =>
      values foreach { value =>
        result.add(name, value)
      }
    }
    result
  }
}
