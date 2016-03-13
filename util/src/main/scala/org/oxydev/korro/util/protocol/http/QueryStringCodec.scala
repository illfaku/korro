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
package org.oxydev.korro.util.protocol.http

import java.net.{URLDecoder, URLEncoder}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object QueryStringCodec {

  /**
   * Decodes query string to list of string pairs (name, value).
   * If name has no value (e.g., "...&name&...") then value in pair will be represented by empty string.
   * @param query query string to decode
   * @param enc encoding for [[java.net.URLDecoder#decode(String, String)]] function
   * @return decoded pairs of names and values
   */
  def decode(query: String, enc: String = "UTF-8"): List[(String, String)] = {
    query.split("[&;]").map(_.split("=", 2)) map {
      case Array(n, v) => URLDecoder.decode(n, enc) -> URLDecoder.decode(v, enc)
      case Array(n) => URLDecoder.decode(n, enc) -> ""
    } toList
  }

  /**
   * Encodes list of string pairs to query string with '&' as separator.
   * @param entries string pairs to encode
   * @param enc encoding for [[java.net.URLEncoder#encode(String, String)]] function
   * @return URL encoded query string
   */
  def encode(entries: List[(String, String)], enc: String = "UTF-8"): String = {
    entries map (e => URLEncoder.encode(e._1, enc) + "=" + URLEncoder.encode(e._2, enc)) mkString "&"
  }
}
