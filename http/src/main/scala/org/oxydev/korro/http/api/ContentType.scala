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

import java.nio.charset.Charset

import scala.util.Try

/**
 * Content-Type header representation.
 */
case class ContentType(mime: String, charset: Option[Charset]) {
  override lazy val toString = charset.map(ch => s"$mime; charset=${ch.name.toLowerCase}").getOrElse(mime)
}

object ContentType {

  /**
   * Constants for some of mime types.
   */
  object Names {
    val TextPlain = "text/plain"
    val ApplicationJson = "application/json"
    val FormUrlEncoded = "application/x-www-form-urlencoded"
    val OctetStream = "application/octet-stream"
  }

  /**
   * Constant for default charset (UTF-8).
   */
  val DefaultCharset = Charset.forName("UTF-8")


  private val regex = """([^;]+)(?:; charset=([\w-]+))?""".r

  /**
   * Tries to extract mime type and charset from Content-Type header.
   */
  def parse(header: String): ContentType = {
    regex.unapplySeq(header) map { l =>
      val mime = l.head
      val charset = l.drop(1).headOption.flatMap(toCharset)
      ContentType(mime, charset)
    } getOrElse ContentType(Names.OctetStream)
  }

  private def toCharset(name: String): Option[Charset] = Try(Charset.forName(name)).toOption

  def apply(mime: String): ContentType = apply(mime, None)

  def apply(mime: String, charset: Charset): ContentType = apply(mime, Some(charset))
}
