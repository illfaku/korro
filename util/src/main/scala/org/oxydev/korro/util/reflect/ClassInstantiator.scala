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
package org.oxydev.korro.util.reflect

import java.lang.reflect.Constructor

import scala.reflect._

/**
 * Utility to create class instances using reflection.
 *
 * @author Vladimir Konstantinov
 */
object ClassInstantiator {

  private val defaults = Map[Class[_], AnyRef](
    classOf[Byte] -> java.lang.Byte.valueOf(0.toByte),
    classOf[Short] -> java.lang.Short.valueOf(0.toShort),
    classOf[Int] -> java.lang.Integer.valueOf(0),
    classOf[Long] -> java.lang.Long.valueOf(0L),
    classOf[Float] -> java.lang.Float.valueOf(0F),
    classOf[Double] -> java.lang.Double.valueOf(0D),
    classOf[Boolean] -> java.lang.Boolean.valueOf(false)
  )

  private def defaultParametersFor(constructor: Constructor[_]): Seq[AnyRef] = {
    constructor.getParameterTypes.map(defaults.getOrElse(_, null))
  }

  /**
   * Creates new class instance with default constructor parameters.
   * @tparam T Class to be instantiated.
   * @return New class instance.
   */
  def instanceOf[T: ClassTag]: T = {
    val clazz = classTag[T].runtimeClass
    require(clazz.getConstructors.nonEmpty, s"Class ${clazz.getName} should have at least one public constructor.")
    try {
      val defaultConstructor = clazz.getConstructor()
      defaultConstructor.newInstance().asInstanceOf[T]
    } catch {
      case e: NoSuchMethodException =>
        val constructor = clazz.getConstructors()(0)
        constructor.newInstance(defaultParametersFor(constructor): _*).asInstanceOf[T]
    }
  }
}
