package io.cafebabe.http.api

import io.cafebabe.http.api.HttpMethod.HttpMethod

import scala.reflect.ClassTag

/**
 * @author Vladimir Konstantinov
 * @version 1.0 (4/14/2015)
 */
trait RestRequest {
  def method: HttpMethod
  def path: String
  def as[T: ClassTag]: T
}
