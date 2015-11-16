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
package io.cafebabe.korro.api.http

import io.cafebabe.korro.util.protocol.http.MimeType

import org.json4s.JValue

import java.nio.charset.Charset
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.StandardOpenOption.{CREATE, TRUNCATE_EXISTING, WRITE}
import java.nio.file.{Files, Path}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
sealed trait HttpContent

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
case class TextHttpContent(text: CharSequence, charset: Charset = Charset.defaultCharset) extends HttpContent

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
case class JsonHttpContent(json: JValue, charset: Charset = Charset.defaultCharset) extends HttpContent

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
case class FileStreamHttpContent(path: Path, pos: Long = 0) extends HttpContent

/**
  * TODO: Add description.
  *
  * @author Vladimir Konstantinov
  */
sealed trait RawHttpContent {
  def contentType: String
  def bytes: Array[Byte]
  def string(charset: Charset = Charset.defaultCharset): String = new String(bytes, charset)
  def save(path: Path): Unit
}

/**
  * TODO: Add description.
  *
  * @author Vladimir Konstantinov
  */
case class MemoryRawHttpContent(contentType: String, bytes: Array[Byte]) extends RawHttpContent {
  override def save(path: Path): Unit = Files.write(path, bytes, CREATE, WRITE, TRUNCATE_EXISTING)
}

/**
  * TODO: Add description.
  *
  * @author Vladimir Konstantinov
  */
case class FileRawHttpContent(contentType: String, file: Path) extends RawHttpContent {
  override lazy val bytes: Array[Byte] = Files.readAllBytes(file)
  override def save(path: Path): Unit = Files.copy(file, path, REPLACE_EXISTING)
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
case object EmptyHttpContent extends HttpContent

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object HttpContent {
  implicit def string2content(text: CharSequence): HttpContent = TextHttpContent(text)
  implicit def jValue2content(json: JValue): HttpContent = JsonHttpContent(json)

  object Text {
    def apply(text: CharSequence, charset: Charset = Charset.defaultCharset): RawHttpContent = {
      //MemoryRawHttpContent(MimeType.Names.)
      ???
    }
  }
}
