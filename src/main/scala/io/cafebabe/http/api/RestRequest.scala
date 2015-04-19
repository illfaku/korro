package io.cafebabe.http.api

import io.cafebabe.http.impl.util.StringCodec

import scala.reflect._

/**
 * @author Vladimir Konstantinov
 * @version 1.0 (4/14/2015)
 */
trait RestRequest {

  def method: String
  def path: String

  def parameters: Map[String, String]
  def parameter[T: ClassTag](name: String): Option[T] = parameters.get(name) map StringCodec.fromString[T]

  def headers: Map[String, String]
  def header[T: ClassTag](name: String): Option[T] = headers.get(name) map StringCodec.fromString[T]

  def content[T: ClassTag]: T
}
