/*
 * Copyright 2016 Vladimir Konstantinov, Yuriy Gintsyak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
