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
package com.github.illfaku.korro.util.reflect

import java.lang.reflect.Constructor

import scala.reflect._

/**
 * Utility to create class instance using reflection.
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

  @inline private def defaultParametersFor(constructor: Constructor[_]): Seq[AnyRef] = {
    constructor.getParameterTypes.map(defaults.getOrElse(_, null))
  }

  /**
   * Creates new class instance with default constructor parameters.
   *
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
