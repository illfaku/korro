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
package io.cafebabe.korro.util.protocol.http

import java.nio.charset.Charset

import scala.util.Try

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object ContentType {

  val DefaultCharset = Charset.forName("UTF-8")

  private val regex = """([^;]+)(?:; charset=([\w-]+))?""".r

  def parse(header: String): ContentType = {
    regex.unapplySeq(header) map { l =>
      val mime = l.head
      val charset = l.drop(1).headOption.flatMap(toCharset).getOrElse(DefaultCharset)
      ContentType(mime, charset)
    } getOrElse ContentType(MimeType.Names.OctetStream, DefaultCharset)
  }

  private def toCharset(name: String): Option[Charset] = Try(Charset.forName(name)).toOption
}

case class ContentType(mime: String, charset: Charset = ContentType.DefaultCharset) {
  lazy val asHeader = s"$mime; charset=${charset.name.toLowerCase}"
}
