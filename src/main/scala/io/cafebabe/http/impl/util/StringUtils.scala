package io.cafebabe.http.impl.util

import scala.reflect._

/**
 * @author Vladimir Konstantinov
 * @version 1.0 (4/12/2015)
 */
object StringUtils {

  case class IsNotPrimitiveException(value: String) extends Exception(value)

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

  def toPrimitive[T: ClassTag](value: String): T = {
    val target = classTag[T].runtimeClass
    try converters(target)(value).asInstanceOf[T] catch {
      case e: NoSuchElementException => throw IsNotPrimitiveException(target.getName)
    }
  }
}
