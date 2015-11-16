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
package io.cafebabe.korro.netty.convert

import java.nio.charset.Charset

import io.cafebabe.korro.api.http._
import io.cafebabe.korro.netty.ByteBufUtils._
import io.cafebabe.korro.netty.{DefaultNettyContent, FileStreamNettyContent, NettyContent}
import io.cafebabe.korro.util.log.Logging
import io.cafebabe.korro.util.protocol.http.ContentType
import io.cafebabe.korro.util.protocol.http.MimeType.Names._
import io.netty.buffer.ByteBuf
import io.netty.handler.codec.http.HttpConstants.DEFAULT_CHARSET
import io.netty.handler.codec.http.HttpHeaders
import io.netty.handler.codec.http.HttpHeaders.Names._
import org.json4s.native.JsonMethods.{compact, render}
import org.json4s.native.JsonParser.parse

import scala.util.{Failure, Success, Try}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object HttpContentConverter extends Logging {

  def fromNetty(content: ByteBuf, headers: HttpHeaders): Either[ConversionFailure, HttpContent] = {
    contentType(headers) match {
      case Some((TextPlain, charset)) =>
        Right(TextHttpContent(content.toString(charset)))
      case Some((ApplicationJson, charset)) =>
        val text = content.toString(charset)
        Try(parse(text)) match {
          case Success(json) => Right(JsonHttpContent(json))
          case Failure(error) => Left(MalformedJson(text))
        }
      case Some((FormUrlEncoded, _)) =>
        Right(EmptyHttpContent) // processed by QueryParamsConverter
      case Some((mime, _)) =>
        Left(UnsupportedContentType(mime))
      case None =>
        Right(EmptyHttpContent)
    }
  }

  private def contentType(headers: HttpHeaders): Option[(String, Charset)] = {
    if (contentLength(headers) > 0) {
      headers.get(CONTENT_TYPE) match {
        case ContentType(mime, charset) => Some(mime -> charset.flatMap(toCharset).getOrElse(DEFAULT_CHARSET))
        case _ => Some(TextPlain -> DEFAULT_CHARSET)
      }
    } else None
  }

  private def contentLength(headers: HttpHeaders): Int = {
    val header = headers.get(CONTENT_LENGTH)
    if (header != null) {
      try header.toInt catch {
        case e: NumberFormatException => 0
      }
    } else 0
  }

  private def toCharset(name: String): Option[Charset] = {
    try Some(Charset.forName(name)) catch { case e: Throwable => None }
  }

  def toNetty(content: HttpContent): NettyContent = content match {
    case TextHttpContent(text, charset) =>
      new DefaultNettyContent(toByteBuf(text, charset), ContentType(TextPlain, charset.name))
    case JsonHttpContent(json, charset) =>
      new DefaultNettyContent(toByteBuf(compact(render(json)), charset), ContentType(ApplicationJson, charset.name))
    case EmptyHttpContent =>
      new DefaultNettyContent(emptyByteBuf, ContentType(TextPlain))
    case FileStreamHttpContent(path, pos) =>
      new FileStreamNettyContent(path, pos)
    case _ => throw new UnsupportedOperationException(s"Unsupported conversion from ${content.getClass}.")
  }
}
