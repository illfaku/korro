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
package io.cafebabe.korro.api.http

import io.cafebabe.util.lang.StringUtils.fromString

import scala.reflect.ClassTag

/**
 * Factory methods for QueryParams class.
 *
 * @author Vladimir Konstantinov
 */
object QueryParams {

  val empty = new QueryParams(Map.empty)

  def apply(parameters: (String, Any)*): QueryParams = {
    val result = parameters.groupBy(_._1).mapValues(_.map(_._2.toString).toList)
    new QueryParams(result)
  }
}

/**
 * Query parameters of HTTP request.
 * Can contain several values for one entry.
 *
 * @author Vladimir Konstantinov
 */
class QueryParams(parameters: Map[String, List[String]]) {
  def one[T: ClassTag](name: String): Option[T] = parameters.get(name).map(_.head).map(fromString)
  def all[T: ClassTag](name: String): List[T] = parameters.get(name).map(_.map(fromString)).getOrElse(List.empty)
  def toMap: Map[String, List[String]] = parameters
  override def toString: String = parameters.mkString("QueryParams(", ", ", ")")
}
