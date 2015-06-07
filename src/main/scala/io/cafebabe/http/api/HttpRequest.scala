package io.cafebabe.http.api

import io.cafebabe.http.impl.util.StringUtils._

import scala.reflect._

/**
 * @author Vladimir Konstantinov
 * @version 1.0 (4/14/2015)
 */
trait HttpRequest {

  def method: String
  def path: String

  def parameters: Map[String, String]
  def primitiveParameter[T: ClassTag](name: String): Option[T] = parameters.get(name) flatMap { value =>
    try Some(toPrimitive(value)) catch { case IsNotPrimitiveException(_) => None }
  }

  def headers: Map[String, String]
  def primitiveHeader[T: ClassTag](name: String): Option[T] = headers.get(name) flatMap { value =>
    try Some(toPrimitive(value)) catch { case IsNotPrimitiveException(_) => None }
  }

  def content[T: ClassTag]: T
}
