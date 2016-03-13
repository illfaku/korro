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

/**
 * Extractor for variables in URI path.
 * <br><br>
 * Example usage:
 * {{{
 *   val request: HttpRequest = ???
 *   object PetInfo extends VariablePath("/category/{}/pet/{}/info")
 *   request match {
 *     case Get(PetInfo(category, pet), r) => ???
 *   }
 * }}}
 * Internally each occurrence of '{}' is replaced with '([&#94;/]+)'
 * and pattern compiles to regular expression.
 *
 * @author Vladimir Konstantinov
 */
abstract class VariablePath(pattern: String) {

  private val regex = pattern.replace("{}", """([^/]+)""").r

  def unapplySeq(path: String): Option[List[String]] = regex.unapplySeq(path)
}
