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

import io.cafebabe.http.server.impl.util.StringUtils._

import java.util

import scala.collection.JavaConversions._
import scala.reflect.ClassTag

/**
 * @author Vladimir Konstantinov
 * @version 1.0 (6/22/2015)
 */
object QueryParams {
  def apply(params: util.Map[String, util.List[String]]): QueryParams = {
    new QueryParams(params.toMap.mapValues(_.toList))
  }
}

/**
 * @author Vladimir Konstantinov
 * @version 1.0 (6/22/2015)
 */
class QueryParams(parameters: Map[String, List[String]]) {
  def one[T: ClassTag](name: String): Option[T] = parameters.get(name).map(_.head).map(fromString)
  def all[T: ClassTag](name: String): List[T] = parameters.get(name).map(_.map(fromString)).getOrElse(Nil)
}
