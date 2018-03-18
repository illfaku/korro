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
package com.github.illfaku.korro.util

import java.net.{URLDecoder, URLEncoder}

/**
 * Query string codec.
 */
object QueryStringCodec {

  private val Encoding = "UTF-8"

  /**
   * Decodes query string to list of string pairs (name, value).
   * If name has no value (e.g., "...&name&...") then value in pair will be represented by empty string.
   *
   * Uses <a href="http://docs.oracle.com/javase/8/docs/api/java/net/URLDecoder.html#decode-java.lang.String-java.lang.String-">
   * `URLDecoder.decode(String, String)`</a> for decoding.
   *
   * @param query Query string to decode.
   * @return Decoded pairs of names and values.
   */
  def decode(query: String): List[(String, String)] = {
    if (query.trim.isEmpty) {
      Nil
    } else {
      query.split("[&;]").map(_.split("=", 2)) map {
        case Array(n, v) => URLDecoder.decode(n, Encoding) -> URLDecoder.decode(v, Encoding)
        case Array(n) => URLDecoder.decode(n, Encoding) -> ""
      } toList
    }
  }

  /**
   * Encodes list of string pairs to query string with defined separator.
   *
   * Uses <a href="http://docs.oracle.com/javase/8/docs/api/java/net/URLEncoder.html#encode-java.lang.String-java.lang.String-">
   * `URLEncoder.encode(String, String)`</a> for encoding.
   *
   * @param entries String pairs to encode.
   * @param separator Pairs separator (`&` - by default).
   * @return URL encoded query string.
   */
  def encode(entries: List[(String, String)], separator: String = "&"): String = {
    entries map (e => URLEncoder.encode(e._1, Encoding) + "=" + URLEncoder.encode(e._2, Encoding)) mkString separator
  }
}
