/*
 * Copyright 2016-2017 Vladimir Konstantinov, Yuriy Gintsyak
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
package com.github.illfaku.korro.api

object HttpVersion {

  val Http10 = apply("HTTP", 1, 0)
  val Http11 = apply("HTTP", 1, 1)

  private val pattern = """(\S+)/(\d+)\.(\d+)""".r

  def parse(text: String): HttpVersion = text match {
    case pattern(protocol, major, minor) => apply(protocol, major.toInt, minor.toInt)
    case _ => throw new IllegalArgumentException("Invalid HTTP version format: " + text)
  }
}

case class HttpVersion(protocol: String, major: Int, minor: Int) {
  override val toString = protocol + "/" + major + "." + minor
}
