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
package org.oxydev.korro.util.protocol.http

import java.net.{URLDecoder, URLEncoder}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object QueryStringUtils {

  /**
   * Splits uri to path and query string.
   * If there is no '?' in uri, then result will be (uri, "").
   * @param uri uri to split
   * @return path and query string as tuple
   */
  def split(uri: String): (String, String) = {
    val pos = uri.indexOf('?')
    if (pos == -1) uri -> "" else uri.substring(0, pos) -> uri.substring(pos + 1)
  }

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
