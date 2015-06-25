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
package io.cafebabe.http.server.impl

import io.cafebabe.http.server.api._
import io.cafebabe.http.server.api.exception.BadRequestException
import io.netty.handler.codec.http.HttpHeaders.Names._
import io.netty.handler.codec.http.{HttpHeaders => NettyHttpHeaders, DefaultHttpHeaders, FullHttpRequest, QueryStringDecoder}
import org.json4s.ParserUtil.ParseException
import org.json4s.native.JsonMethods._

import java.nio.charset.Charset

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object HttpRequestConverter {
  def fromNetty(request: FullHttpRequest, pathPrefix: String): HttpRequest = {
    val path = new QueryStringDecoder(request.getUri).path
    HttpRequest(
      request.getMethod.name,
      path.substring(pathPrefix.length),
      QueryParamsConverter.fromNetty(request),
      HttpHeadersConverter.fromNetty(request),
      HttpContentConverter.fromNetty(request)
    )
  }
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object QueryParamsConverter {
  def fromNetty(request: FullHttpRequest): QueryParams = {
    val uri = new QueryStringDecoder(request.getUri)
    val uriParams = uri.parameters.toMap.mapValues(_.toList)
    // TODO: application/x-www-form-urlencoded
    new QueryParams(uriParams)
  }
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object HttpHeadersConverter {

  def fromNetty(request: FullHttpRequest): HttpHeaders = {
    val result = mutable.Map.empty[String, List[String]]
    for (header <- request.headers) {
      val key = header.getKey
      val list = result.getOrElse(key, List.empty)
      result += key -> (header.getValue :: list)
    }
    new HttpHeaders(Map.empty ++ result)
  }

  def toNetty(headers: HttpHeaders): NettyHttpHeaders = {
    val result = new DefaultHttpHeaders
    headers.map foreach { case (name, values) =>
      values foreach { value =>
        result.add(name, value)
      }
    }
    result
  }
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object HttpContentConverter {

  private val DefaultCharset = Charset.forName("UTF-8")

  private val ContentType = """([^;]+)(?:; charset=([\w-]+))""".r

  def fromNetty(request: FullHttpRequest): HttpContent = {
    if (contentLength(request) > 0) {
      contentType(request) match {
        case ("text/plain", charset) => TextHttpContent(request.content.toString(charset))
        case ("application/json", charset) =>
          try JsonHttpContent(parse(request.content.toString(charset))) catch {
            case e: ParseException => throw new BadRequestException(s"Fail to parse json content: ${e.getMessage}")
          }
        case (mime, _) => throw new BadRequestException(s"Unsupported Content-Type: $mime.")
      }
    } else EmptyHttpContent
  }

  private def contentLength(request: FullHttpRequest): Int = {
    val header = request.headers.get(CONTENT_LENGTH)
    if (header != null) {
      try header.toInt catch {
        case e: NumberFormatException => 0
      }
    } else 0
  }

  private def contentType(request: FullHttpRequest): (String, Charset) = {
    request.headers.get(CONTENT_TYPE) match {
      case ContentType(mime, charset) =>
        if (charset != null) {
          try mime -> Charset.forName(charset) catch {
            case e: IllegalArgumentException => throw new BadRequestException(s"Unsupported charset: $charset.")
          }
        } else mime -> DefaultCharset
      case _ => "text/plain" -> DefaultCharset
    }
  }
}
