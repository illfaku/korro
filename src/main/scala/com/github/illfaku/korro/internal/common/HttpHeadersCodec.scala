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
package com.github.illfaku.korro.internal.common

import com.github.illfaku.korro.dto.{HttpContent, HttpParams}

import io.netty.handler.codec.http.{DefaultHttpHeaders, HttpHeaderNames, HttpHeaders}

import scala.collection.JavaConverters._

private[internal] object HttpHeadersCodec {

  def encode(headers: HttpParams, content: HttpContent): HttpHeaders = {

    val result = new DefaultHttpHeaders()

    headers.entries foreach { case (name, value) => result.add(name, value) }

    if (content.contentLength > 0) {
      if (!result.contains(HttpHeaderNames.CONTENT_LENGTH)) {
        result.add(HttpHeaderNames.CONTENT_LENGTH, content.contentLength)
      }
      if (!result.contains(HttpHeaderNames.CONTENT_TYPE)) {
        content.contentType foreach { t => result.add(HttpHeaderNames.CONTENT_TYPE, t.toString) }
      }
    }

    result
  }

  def decode(headers: HttpHeaders): HttpParams = {
    new HttpParams(headers.asScala.map(header => header.getKey -> header.getValue).toList)
  }
}
