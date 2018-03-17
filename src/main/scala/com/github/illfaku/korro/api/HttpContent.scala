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
package com.github.illfaku.korro.api

import com.github.illfaku.korro.util.net.{MimeTypes, QueryStringCodec}

import java.nio.charset.{Charset, StandardCharsets}
import java.nio.file.{Files, Paths}

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
case class MemoryHttpContent(bytes: Array[Byte], contentType: Option[ContentType]) extends HttpContent {

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

  override val toString = s"MemoryHttpContent(contentType=$contentType, length=$length)"
}

/**
 * HTTP message body representation stored in file.
 *
 * @param path Path to file storing data of HTTP content.
 * @param contentType Optional content type from/for HTTP message headers.
 */
case class FileHttpContent(path: String, length: Long, contentType: Option[ContentType]) extends HttpContent {
  override val toString = s"FileHttpContent(contentType=$contentType, path=$path)"
}

case class ChunkedHttpContent(id: Int, contentType: Option[ContentType]) extends HttpContent {
  override val toString = s"ChunkedHttpContent(contentType=$contentType, id=$id)"
}

/**
 * Factory methods of HttpContent.
 */
object HttpContent {

  val empty: HttpContent = Memory(Array.emptyByteArray)

  object Memory {
    def apply(bytes: Array[Byte]): HttpContent = apply(bytes, None)
    def apply(bytes: Array[Byte], contentType: ContentType): HttpContent = apply(bytes, Option(contentType))
    def apply(bytes: Array[Byte], contentType: Option[ContentType]): HttpContent = {
      MemoryHttpContent(bytes, contentType)
    }
    def unapply(msg: HttpMessage): Option[Array[Byte]] = msg.content match {
      case c: MemoryHttpContent => Option(c.bytes)
      case _ => None
    }
  }

  /**
   * Factory and extractor for content of type `text/plain`.
   */
  object Text {
    def apply(text: String, charset: Charset = StandardCharsets.UTF_8): HttpContent = {
      Memory(text.getBytes(charset), ContentType(TextPlain, charset))
    }
    def unapply(msg: HttpMessage): Option[String] = msg.content match {
      case c: MemoryHttpContent => Option(c.string)
      case _ => None
    }
  }

  /**
   * Factory and extractor for content of type `application/json`.
   */
  object Json {
    def apply(json: JValue, charset: Charset = StandardCharsets.UTF_8): HttpContent = {
      Memory(compact(render(json)).getBytes(charset), ContentType(ApplicationJson, charset))
    }
    def unapply(msg: HttpMessage): Option[JValue] = Text.unapply(msg).flatMap(parseOpt)
  }

  /**
   * Factory and extractor for content of type `application/x-www-form-urlencoded`.
   */
  object Form {
    def apply(entries: (String, Any)*): HttpContent = {
      val encoded = QueryStringCodec.encode(entries.map(e => e._1 -> e._2.toString).toList)
      Memory(encoded.getBytes(StandardCharsets.UTF_8), ContentType(FormUrlEncoded))
    }
    def unapply(msg: HttpMessage): Option[HttpParams] = Text.unapply(msg) map { body =>
      val decoded = QueryStringCodec.decode(body)
      new HttpParams(decoded)
    }
  }

  object File {
    def apply(path: String): HttpContent = apply(path, ContentType(MimeTypes(path)))
    def apply(path: String, contentType: ContentType): HttpContent = apply(path, Option(contentType))
    def apply(path: String, contentType: Option[ContentType]): HttpContent = {
      apply(path, Files.size(Paths.get(path)), contentType)
    }
    def apply(path: String, length: Long): HttpContent = apply(path, length, ContentType(MimeTypes(path)))
    def apply(path: String, length: Long, contentType: ContentType): HttpContent = apply(path, length, Option(contentType))
    def apply(path: String, length: Long, contentType: Option[ContentType]): HttpContent = {
      FileHttpContent(path, length, contentType)
    }
    def unapply(msg: HttpMessage): Option[String] = msg.content match {
      case c: FileHttpContent => Option(c.path)
      case _ => None
    }
  }

  object Chunked {
    def apply(id: Int): HttpContent = apply(id, None)
    def apply(id: Int, contentType: ContentType): HttpContent = apply(id, Option(contentType))
    def apply(id: Int, contentType: Option[ContentType]): HttpContent = {
      ChunkedHttpContent(id, contentType)
    }
    def unapply(msg: HttpMessage): Option[Int] = msg.content match {
      case c: ChunkedHttpContent => Option(c.id)
      case _ => None
    }
  }
}
