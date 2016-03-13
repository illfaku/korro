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
  def parse(header: String): Option[ContentType] = regex.unapplySeq(header) map { l =>
    val mime = l.head
    val charset = l.drop(1).headOption.flatMap(toCharset)
    ContentType(mime, charset)
  }

  private def toCharset(name: String): Option[Charset] = Try(Charset.forName(name)).toOption

  def apply(mime: String): ContentType = apply(mime, None)

  def apply(mime: String, charset: Charset): ContentType = apply(mime, Some(charset))
}
