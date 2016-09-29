/*
 * Copyright 2016 Vladimir Konstantinov, Yuriy Gintsyak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.oxydev.korro.http.api

import org.oxydev.korro.http.api.ContentType.Names.{ApplicationJson, FormUrlEncoded, TextPlain}
import org.oxydev.korro.util.net.QueryStringCodec

import org.json4s.JValue
import org.json4s.native.JsonMethods.{compact, render}
import org.json4s.native.JsonParser.parseOpt

import java.nio.charset.{Charset, StandardCharsets}

/**
 * HTTP message body representation.
 */
sealed trait HttpContent {

  /**
   * Optional content type from/for HTTP message headers.
   */
  def contentType: Option[ContentType]
}

/**
 * HTTP message body representation stored in memory.
 *
 * @param bytes Binary data.
 * @param contentType Optional content type from/for HTTP message headers.
 */
class MemoryHttpContent(val bytes: Array[Byte], val contentType: Option[ContentType]) extends HttpContent {

  /**
   * Length of this content.
   */
  val length: Long = bytes.length

  /**
   * Converts binary data of this content to String using charset from content type or `UTF-8` if there is no one.
   */
  def string: String = string(contentType.flatMap(_.charset).getOrElse(StandardCharsets.UTF_8))

  /**
   * Converts binary data of this content to String using provided charset.
   */
  def string(charset: Charset): String = new String(bytes, charset)

  override lazy val toString: String = s"MemoryHttpContent(contentType=$contentType, length=$length)"
}

/**
 * HTTP message body representation stored in file.
 *
 * @param path Path to file storing data of HTTP content.
 * @param contentType Optional content type from/for HTTP message headers.
 */
class FileHttpContent(val path: String, val contentType: Option[ContentType]) extends HttpContent {
  override lazy val toString: String = s"FileHttpContent(contentType=$contentType, path=$path)"
}

/**
 * Factory methods of HttpContent.
 */
object HttpContent {

  val empty: HttpContent = memory(Array.emptyByteArray)

  def memory(bytes: Array[Byte]): HttpContent = memory(bytes, None)
  def memory(bytes: Array[Byte], contentType: ContentType): HttpContent = memory(bytes, Some(contentType))
  def memory(bytes: Array[Byte], contentType: Option[ContentType]): HttpContent = {
    new MemoryHttpContent(bytes, contentType)
  }

  def file(path: String): HttpContent = file(path, None)
  def file(path: String, contentType: ContentType): HttpContent = file(path, Some(contentType))
  def file(path: String, contentType: Option[ContentType]): HttpContent = new FileHttpContent(path, contentType)


  /**
   * Factory and extractor for content of type `text/plain`.
   */
  object Text {
    def apply(text: String, charset: Charset = StandardCharsets.UTF_8): HttpContent = {
      memory(text.getBytes(charset), ContentType(TextPlain, charset))
    }
    def unapply(msg: HttpMessage): Option[String] = msg.content match {
      case c: MemoryHttpContent => Some(c.string)
      case _ => None
    }
  }

  /**
   * Factory and extractor for content of type `application/json`.
   */
  object Json {
    def apply(json: JValue, charset: Charset = StandardCharsets.UTF_8): HttpContent = {
      memory(compact(render(json)).getBytes(charset), ContentType(ApplicationJson, charset))
    }
    def unapply(msg: HttpMessage): Option[JValue] = Text.unapply(msg).flatMap(parseOpt)
  }

  /**
   * Factory and extractor for content of type `application/x-www-form-urlencoded`.
   */
  object Form {
    def apply(entries: (String, Any)*): HttpContent = {
      val encoded = QueryStringCodec.encode(entries.map(e => e._1 -> e._2.toString).toList)
      memory(encoded.getBytes(StandardCharsets.UTF_8), ContentType(FormUrlEncoded))
    }
    def unapply(msg: HttpMessage): Option[HttpParams] = Text.unapply(msg) map { body =>
      val decoded = QueryStringCodec.decode(body)
      new HttpParams(decoded)
    }
  }
}
