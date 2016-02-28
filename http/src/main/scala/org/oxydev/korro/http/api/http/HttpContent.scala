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
package org.oxydev.korro.http.api.http

import org.oxydev.korro.http.api.http.ContentType.DefaultCharset
import org.oxydev.korro.http.api.http.ContentType.Names.{ApplicationJson, FormUrlEncoded, OctetStream, TextPlain}
import org.oxydev.korro.util.protocol.http.MimeTypeMapping.getMimeType

import org.json4s.JValue
import org.json4s.native.JsonMethods.{compact, render}
import org.json4s.native.JsonParser.parseOpt

import java.net.URLEncoder
import java.nio.charset.Charset
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.StandardOpenOption.{CREATE, TRUNCATE_EXISTING, WRITE}
import java.nio.file.{Files, Path}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
sealed trait HttpContent {
  def contentType: ContentType
  def length: Long
  def bytes: Array[Byte]
  def string: String = new String(bytes, contentType.charset.getOrElse(DefaultCharset))
  def save(path: Path): Unit
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class MemoryHttpContent(val bytes: Array[Byte], val contentType: ContentType) extends HttpContent {
  override val length: Long = bytes.length
  override def save(path: Path): Unit = Files.write(path, bytes, CREATE, WRITE, TRUNCATE_EXISTING)

  override lazy val toString: String = s"MemoryHttpContent(contentType=$contentType, length=$length)"
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class FileHttpContent(val file: Path, val contentType: ContentType, val length: Long) extends HttpContent {
  override lazy val bytes: Array[Byte] = Files.readAllBytes(file)
  override def save(path: Path): Unit = Files.copy(file, path, REPLACE_EXISTING)

  override lazy val toString: String = s"FileHttpContent(contentType=$contentType, length=$length, path=$file)"
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object HttpContent {

  val empty: HttpContent = new MemoryHttpContent(Array.emptyByteArray, ContentType(OctetStream))

  def memory(bytes: Array[Byte], contentType: ContentType): HttpContent = new MemoryHttpContent(bytes, contentType)

  def file(path: Path): HttpContent = {
    file(path, ContentType(getMimeType(path).getOrElse(OctetStream)), Files.size(path))
  }
  def file(path: Path, contentType: ContentType): HttpContent = {
    file(path, contentType, Files.size(path))
  }
  def file(path: Path, contentType: ContentType, length: Long): HttpContent = {
    new FileHttpContent(path, contentType, length)
  }

  def form(entries: (String, Any)*): HttpContent = {
    val encoded = entries map {
      case (name, value) => URLEncoder.encode(name, "UTF-8") + "=" + URLEncoder.encode(value.toString, "UTF-8")
    } mkString "&"
    memory(encoded.getBytes(DefaultCharset), ContentType(FormUrlEncoded))
  }


  object Text {
    def apply(text: CharSequence, charset: Charset = DefaultCharset): HttpContent = {
      memory(text.toString.getBytes(charset), ContentType(TextPlain, charset))
    }
    def unapply(msg: HttpMessage): Option[String] = if (msg.content.length > 0) Some(msg.content.string) else None
  }

  object Json {
    def apply(json: JValue, charset: Charset = DefaultCharset): HttpContent = {
      memory(compact(render(json)).getBytes(charset), ContentType(ApplicationJson, charset))
    }
    def unapply(msg: HttpMessage): Option[JValue] = Text.unapply(msg).flatMap(parseOpt)
  }
}
