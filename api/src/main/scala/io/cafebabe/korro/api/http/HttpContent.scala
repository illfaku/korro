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

import org.json4s.JValue

import java.io.File
import java.nio.charset.Charset
import java.nio.file.{Paths, Path}

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
case object EmptyHttpContent extends HttpContent

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object HttpContent {
  implicit def string2content(text: CharSequence): HttpContent = TextHttpContent(text)
  implicit def jValue2content(json: JValue): HttpContent = JsonHttpContent(json)
  implicit def file2content(file: File): HttpContent = FileStreamHttpContent(Paths.get(file.toURI))
  implicit def path2content(path: Path): HttpContent = FileStreamHttpContent(path)
}
