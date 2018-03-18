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

object HttpVersion {

  val Http10 = new HttpVersion("HTTP", 1, 0)
  val Http11 = new HttpVersion("HTTP", 1, 1)

  private val pattern = """(\S+)/(\d+)\.(\d+)""".r

  def parse(text: String): HttpVersion = text match {
    case pattern(protocol, major, minor) => new HttpVersion(protocol, major.toInt, minor.toInt)
    case _ => throw new IllegalArgumentException(s"Invalid HTTP version format: $text")
  }
}

class HttpVersion private[korro] (val protocol: String, val major: Int, val minor: Int) {
  override val toString = s"$protocol/$major.$minor"
}
