package io.cafebabe.http.impl.util

import com.google.gson.Gson

/**
 * @author Vladimir Konstantinov
 * @version 1.0 (4/12/2015)
 */
object StringCodec {

  private val gson = new Gson

  private val converters = Map[Class[_], (String) => AnyRef](
    classOf[String] -> { value: String => value },

    classOf[Byte] -> java.lang.Byte.valueOf,
    classOf[Short] -> java.lang.Short.valueOf,
    classOf[Int] -> java.lang.Integer.valueOf,
    classOf[Long] -> java.lang.Long.valueOf,
    classOf[Float] -> java.lang.Float.valueOf,
    classOf[Double] -> java.lang.Double.valueOf,
    classOf[Boolean] -> java.lang.Boolean.valueOf,

    classOf[java.lang.Byte] -> java.lang.Byte.valueOf,
    classOf[java.lang.Short] -> java.lang.Short.valueOf,
    classOf[java.lang.Integer] -> java.lang.Integer.valueOf,
    classOf[java.lang.Long] -> java.lang.Long.valueOf,
    classOf[java.lang.Float] -> java.lang.Float.valueOf,
    classOf[java.lang.Double] -> java.lang.Double.valueOf,
    classOf[java.lang.Boolean] -> java.lang.Boolean.valueOf
  )

  def fromString(value: String, target: Class[_]): AnyRef = {
    converters.getOrElse(target, { v: String => gson.fromJson(v, target).asInstanceOf[AnyRef] })(value)
  }

  def toString(value: Any): String = {
    if (converters.contains(value.getClass)) value.toString else gson.toJson(value)
  }
}
