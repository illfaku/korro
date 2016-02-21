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
package org.oxydev.korro.util.lang

import scala.reflect._

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
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
    classOf[java.lang.Boolean] -> java.lang.Boolean.valueOf
  )

  def fromString[T: ClassTag](value: String): T = {
    val target = classTag[T].runtimeClass
    converters.get(target).map(_(value).asInstanceOf[T]).getOrElse(throw new IllegalArgumentException(target.getName))
  }
}
