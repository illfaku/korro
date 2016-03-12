/*
 * Copyright (C) 2015, 2016  Vladimir Konstantinov, Yuriy Gintsyak
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
package org.oxydev.korro.http.api

import org.oxydev.korro.http.api.ContentType.DefaultCharset
import org.oxydev.korro.http.api.ContentType.Names.{ApplicationJson, FormUrlEncoded, TextPlain}
import org.oxydev.korro.util.protocol.http.QueryStringCodec

import org.json4s.JValue
import org.json4s.native.JsonMethods.{compact, render}
import org.json4s.native.JsonParser.parseOpt

import java.nio.charset.Charset
import java.nio.file.{Files, Paths}

/**
 * HTTP message body representation.
 */
sealed trait HttpContent {

  /**
   * Optional content type from/for HTTP message headers.
   */
  def contentType: Option[ContentType]

  /**
   * Size of data (in bytes).
   */
  def length: Long

  /**
   * Extracts binary data.
   */
  def bytes: Array[Byte]

  /**
   * Converts binary data of this content to String using charset from content type or
   * [[ContentType#DefaultCharset]] if there is no one.
   */
  def string: String = string(contentType.flatMap(_.charset).getOrElse(DefaultCharset))

  /**
   * Converts binary data of this content to String using provided charset.
   */
  def string(charset: Charset): String = new String(bytes, charset)
}

/**
 * HTTP message body representation stored in memory.
 *
 * @param bytes Binary data.
 * @param contentType Optional content type from/for HTTP message headers.
 */
class MemoryHttpContent(val bytes: Array[Byte], val contentType: Option[ContentType]) extends HttpContent {

  /**
   * Length of [[bytes]].
   */
  override val length: Long = bytes.length

  override lazy val toString: String = s"MemoryHttpContent(contentType=$contentType, length=$length)"
}

/**
 * HTTP message body representation stored in file.
 *
 * @param path Path to file storing data of HTTP content.
 * @param contentType Optional content type from/for HTTP message headers.
 * @param length Size of data (in bytes).
 */
class FileHttpContent(val path: String, val contentType: Option[ContentType], val length: Long) extends HttpContent {

  /**
   * Reads all bytes from file at [[path]].
   */
  override lazy val bytes: Array[Byte] = Files.readAllBytes(Paths.get(path))

  override lazy val toString: String = s"FileHttpContent(contentType=$contentType, length=$length, path=$path)"
}

/**
 * Factories of HttpContent.
 */
object HttpContent {

  val empty: HttpContent = new MemoryHttpContent(Array.emptyByteArray, None)

  def memory(bytes: Array[Byte]): HttpContent = memory(bytes, None)
  def memory(bytes: Array[Byte], contentType: ContentType): HttpContent = memory(bytes, Some(contentType))
  def memory(bytes: Array[Byte], contentType: Option[ContentType]): HttpContent = {
    new MemoryHttpContent(bytes, contentType)
  }

  def file(path: String): HttpContent = file(path, None)
  def file(path: String, contentType: ContentType): HttpContent = file(path, Some(contentType))
  def file(path: String, contentType: Option[ContentType]): HttpContent = file(path, contentType, Files.size(Paths.get(path)))
  def file(path: String, length: Long): HttpContent = file(path, None, length)
  def file(path: String, contentType: ContentType, length: Long): HttpContent = file(path, Some(contentType), length)
  def file(path: String, contentType: Option[ContentType], length: Long): HttpContent = {
    new FileHttpContent(path, contentType, length)
  }


  object Text {
    def apply(text: String, charset: Charset = DefaultCharset): HttpContent = {
      memory(text.getBytes(charset), ContentType(TextPlain, charset))
    }
    def unapply(msg: HttpMessage): Option[String] = Some(msg.content.string)
  }

  object Json {
    def apply(json: JValue, charset: Charset = DefaultCharset): HttpContent = {
      memory(compact(render(json)).getBytes(charset), ContentType(ApplicationJson, charset))
    }
    def unapply(msg: HttpMessage): Option[JValue] = parseOpt(msg.content.string)
  }

  object Form {
    def apply(entries: (String, Any)*): HttpContent = {
      val encoded = QueryStringCodec.encode(entries.map(e => e._1 -> e._2.toString).toList)
      memory(encoded.getBytes(DefaultCharset), ContentType(FormUrlEncoded))
    }
    def unapply(msg: HttpMessage): Option[HttpParams] = {
      val decoded = QueryStringCodec.decode(msg.content.string)
      Some(new HttpParams(decoded))
    }
  }
}
