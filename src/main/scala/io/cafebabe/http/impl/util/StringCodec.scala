package io.cafebabe.http.impl.util

import scala.reflect._

/**
 * @author Vladimir Konstantinov
 * @version 1.0 (4/12/2015)
 */
object StringCodec {

  class IsNotPrimitiveException(value: String) extends Exception(value)

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

  def fromString[T: ClassTag](value: String): T = {
    val target = classTag[T].runtimeClass
    try converters(target)(value).asInstanceOf[T] catch {
      case e: NoSuchElementException => throw new IsNotPrimitiveException(target.getName)
    }
  }

  def toString(value: Any): String =
    if (converters.contains(value.getClass)) value.toString
    else throw new IsNotPrimitiveException(value.toString)
}
