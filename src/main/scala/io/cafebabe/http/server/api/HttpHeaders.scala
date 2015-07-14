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

import io.cafebabe.util.lang.StringUtils.fromString

import scala.collection.mutable
import scala.reflect.ClassTag

/**
 * Factory methods for HttpHeaders class.
 *
 * @author Vladimir Konstantinov
 */
object HttpHeaders {

  val empty = new HttpHeaders(Map.empty)

  def apply(headers: (String, Any)*): HttpHeaders = {
    val result = mutable.Map.empty[String, List[String]]
    headers foreach { case (key, value) =>
      val list = value.toString :: result.getOrElse(key, List.empty)
      result += key -> list
    }
    new HttpHeaders(Map.empty ++ result)
  }
}

/**
 * Headers of HTTP request/response.
 * Can contain several values for one entry.
 *
 * @author Vladimir Konstantinov
 */
class HttpHeaders(headers: Map[String, List[String]]) {
  def one[T: ClassTag](name: String): Option[T] = headers.get(name).map(_.head).map(fromString)
  def all[T: ClassTag](name: String): List[T] = headers.get(name).map(_.map(fromString)).getOrElse(List.empty)
  def toMap: Map[String, List[String]] = headers
  override def toString: String = headers.mkString("HttpHeaders(", ", ", ")")
}
