/*
 * Copyright 2018 Vladimir Konstantinov
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
package com.github.illfaku.korro.dto

import java.nio.charset.Charset

import scala.util.Try

/**
 * Content-Type header representation.
 */
case class ContentType(mime: String, charsetName: Option[String]) {
  def charset: Option[Charset] = charsetName.flatMap(ch => Try(Charset.forName(ch)).toOption)
  override val toString = charsetName.map(ch => s"$mime; charset=$ch").getOrElse(mime)
}

object ContentType {

  /**
   * Constants for some of mime types.
   */
  object Names {
    val TextPlain = "text/plain"
    val TextCsv = "text/csv"
    val TextHtml = "text/html"
    val ApplicationJson = "application/json"
    val FormUrlEncoded = "application/x-www-form-urlencoded"
    val OctetStream = "application/octet-stream"
  }


  private val regex = """([^; ]+) *(?:; *charset=([\w-]+))?""".r

  /**
   * Tries to extract mime type and charset from Content-Type header using regular expression
   * {{{
   *   ([^; ]+) *(?:; *charset=([\w-]+))?
   * }}}
   */
  def parse(header: String): Option[ContentType] = regex.unapplySeq(header.trim) map { l =>
    ContentType(l.head, l.drop(1).headOption)
  }

  def apply(mime: String): ContentType = ContentType(mime, None)

  def apply(mime: String, charset: String): ContentType = ContentType(mime, Option(charset))

  def apply(mime: String, charset: Charset): ContentType = ContentType(mime, Option(charset).map(_.name))
}
