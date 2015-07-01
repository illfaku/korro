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
package io.cafebabe.http.server.impl.convert

import io.cafebabe.http.server.api.exception.BadRequestException
import io.cafebabe.http.server.api.{EmptyHttpContent, HttpContent, JsonHttpContent, TextHttpContent}
import io.cafebabe.http.server.impl.util.ByteBufUtils._
import io.cafebabe.http.server.impl.util.MimeTypes._
import io.cafebabe.http.server.impl.util.{ContentType, StringUtils}

import io.netty.buffer.ByteBuf
import io.netty.handler.codec.http.HttpConstants.DEFAULT_CHARSET
import io.netty.handler.codec.http.HttpHeaders.Names._
import io.netty.handler.codec.http.{DefaultHttpHeaders, HttpHeaders}
import org.json4s.ParserUtil.ParseException
import org.json4s.native.JsonParser._

import java.nio.charset.Charset

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object HttpContentConverter {

  def fromNetty(content: ByteBuf, headers: HttpHeaders): HttpContent = {
    if (contentLength(headers) > 0) extractContent(content, headers)
    else EmptyHttpContent
  }

  def toNetty(content: HttpContent): (ByteBuf, HttpHeaders) = {
    val headers = new DefaultHttpHeaders
    val buf = content match {
      case TextHttpContent(text) =>
        headers.add(CONTENT_TYPE, ContentType(TextPlain))
        toByteBuf(text)
      case JsonHttpContent(json) =>
        headers.add(CONTENT_TYPE, ContentType(ApplicationJson))
        toByteBuf(StringUtils.toString(json))
      case EmptyHttpContent => emptyByteBuf
    }
    headers.add(CONTENT_LENGTH, buf.readableBytes)
    buf -> headers
  }

  private def contentLength(headers: HttpHeaders): Int = {
    val header = headers.get(CONTENT_LENGTH)
    if (header != null) {
      try header.toInt catch {
        case e: NumberFormatException => 0
      }
    } else 0
  }

  private def contentType(headers: HttpHeaders): (String, Charset) = {
    headers.get(CONTENT_TYPE) match {
      case ContentType(mime, charset) =>
        try mime -> charset.map(Charset.forName).getOrElse(DEFAULT_CHARSET) catch {
          case e: IllegalArgumentException => throw new BadRequestException(s"Unsupported charset: $charset.")
        }
      case _ => TextPlain -> DEFAULT_CHARSET
    }
  }

  private def extractContent(content: ByteBuf, headers: HttpHeaders): HttpContent = contentType(headers) match {
    case (TextPlain, charset) => TextHttpContent(content.toString(charset))
    case (ApplicationJson, charset) =>
      try JsonHttpContent(parse(content.toString(charset))) catch {
        case e: ParseException => throw new BadRequestException(s"Fail to parse json content: ${e.getMessage}")
      }
    case (FormUrlEncoded, _) => EmptyHttpContent // processed by QueryParamsConverter
    case (mime, _) => throw new BadRequestException(s"Unsupported Content-Type: $mime.")
  }
}
