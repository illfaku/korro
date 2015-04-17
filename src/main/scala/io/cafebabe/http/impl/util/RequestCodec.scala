package io.cafebabe.http.impl.util

import io.cafebabe.http.api.{RequestQueryParameter, HttpHeader}
import io.cafebabe.util.i18n.Locales
import io.cafebabe.util.reflect.ClassInstantiator._
import io.netty.handler.codec.http.{FullHttpRequest, QueryStringDecoder}

import java.util.Locale

import scala.reflect._

/**
 * @author Vladimir Konstantinov
 * @version 1.0 (4/12/2015)
 */
object RequestCodec {

  def fromHttpRequest[T: ClassTag](request: FullHttpRequest): T = {
    val params = new QueryStringDecoder(request.getUri).parameters
    val headers = request.headers

    val result = instanceOf[T]

    val reqClass = classTag[T].runtimeClass
    reqClass.getFields foreach { field =>
      field.setAccessible(true)
      if (field.getType == classOf[Locale]) {
        val header = headers.get("Accept-Language")
        field.set(result, Locales(header))
      } else if (field.isAnnotationPresent(classOf[HttpHeader])) {
        val headerInfo = field.getAnnotation(classOf[HttpHeader])
        val header = if (headerInfo.value.isEmpty) {
          headers.get(field.getName)
        } else {
          headers.get(headerInfo.value)
        }
        if (header != null) {
          field.set(result, StringCodec.fromString(header, field.getType))
        }
      } else {
        val paramName = Option(field.getAnnotation(classOf[RequestQueryParameter])).map(_.value).getOrElse("")
        val param = if (paramName.isEmpty) {
          params.get(field.getName)
        } else {
          params.get(paramName)
        }
        if (param != null) {
          field.set(result, StringCodec.fromString(param.get(0), field.getType))
        }
      }
    }
    result
  }
}
