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

object HttpHeaders {

  object Names {
    val AcceptLanguage = "Accept-Language"
    val Authorization = "Authorization"
    val CacheControl = "Cache-Control"
    val Connection = "Connection"
    val ContentLength = "Content-Length"
    val ContentType = "Content-Type"
    val Date = "Date"
    val Host = "Host"
    val Location = "Location"
    val Pragma = "Pragma"
    val Upgrade = "Upgrade"
    val UserAgent = "User-Agent"
    val WwwAuthenticate = "WWW-Authenticate"
  }

  def unapply(msg: HttpMessage): Option[HttpParams] = Some(msg.headers)
}
