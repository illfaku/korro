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
package io.cafebabe.http.server.impl.util

import org.json4s._
import org.json4s.native.JsonMethods._
import io.cafebabe.util.json.formats

import scala.reflect._

/**
 * @author Vladimir Konstantinov
 * @version 1.0 (4/12/2015)
 */
object StringUtils {

  private val converters = Map[Class[_], (String) => Any](

    classOf[String] -> { value: String => value },

    classOf[Byte] -> java.lang.Byte.parseByte,
    classOf[Short] -> java.lang.Short.parseShort,
    classOf[Int] -> java.lang.Integer.parseInt,
    classOf[Long] -> java.lang.Long.parseLong,
    classOf[Float] -> java.lang.Float.parseFloat,
    classOf[Double] -> java.lang.Double.parseDouble,
    classOf[Boolean] -> java.lang.Boolean.parseBoolean,

    classOf[java.lang.Byte] -> java.lang.Byte.valueOf,
    classOf[java.lang.Short] -> java.lang.Short.valueOf,
    classOf[java.lang.Integer] -> java.lang.Integer.valueOf,
    classOf[java.lang.Long] -> java.lang.Long.valueOf,
    classOf[java.lang.Float] -> java.lang.Float.valueOf,
    classOf[java.lang.Double] -> java.lang.Double.valueOf,
    classOf[java.lang.Boolean] -> java.lang.Boolean.valueOf,

    classOf[JValue] -> { value: String => parse(value) }
  )

  def fromString[T: ClassTag](value: String): T = {
    val target = classTag[T].runtimeClass
    converters.get(target).map(_(value).asInstanceOf[T]).getOrElse(throw new IllegalArgumentException(target.getName))
  }

  def toString(value: Any): String = value match {

    case v: String => v

    case v: Byte => v.toString
    case v: Short => v.toString
    case v: Int => v.toString
    case v: Long => v.toString
    case v: Float => v.toString
    case v: Double => v.toString
    case v: Boolean => v.toString

    case v: java.lang.Byte => v.toString
    case v: java.lang.Short => v.toString
    case v: java.lang.Integer => v.toString
    case v: java.lang.Long => v.toString
    case v: java.lang.Float => v.toString
    case v: java.lang.Double => v.toString
    case v: java.lang.Boolean => v.toString

    case v: JValue => compact(render(v))

    case _ => throw new IllegalArgumentException(value.getClass.getName)
  }
}
