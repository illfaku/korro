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
package io.cafebabe.http.server.api

import io.cafebabe.http.server.api.exception.BadRequestException
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaders.Names._
import org.json4s.JValue
import org.json4s.ParserUtil.ParseException
import org.json4s.native.JsonMethods.parse

import java.nio.charset.Charset

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 * @version 1.0 (6/23/2015)
 */
object HttpContent {

  private val DefaultCharset = Charset.forName("UTF-8")

  private val ContentType = """([^;]+)(?:; charset=([\w-]+))""".r

  private[server] def apply(request: FullHttpRequest): HttpContent = {
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

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 * @version 1.0 (6/23/2015)
 */
sealed trait HttpContent

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 * @version 1.0 (6/23/2015)
 */
case class TextHttpContent(text: String) extends HttpContent

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 * @version 1.0 (6/23/2015)
 */
case class JsonHttpContent(json: JValue) extends HttpContent

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 * @version 1.0 (6/23/2015)
 */
case object EmptyHttpContent extends HttpContent
