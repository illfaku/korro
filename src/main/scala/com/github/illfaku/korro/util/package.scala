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
package com.github.illfaku.korro

import com.typesafe.config.Config

package object util {

  /**
   * Concatenates 2 extractors in a case statement.
   */
  object & {
    def unapply[A](a: A) = Some(a, a)
  }

  /**
   * Converts <a href="http://lightbend.github.io/config/latest/api/com/typesafe/config/Config.html">`Config`</a>
   * object to [[com.github.illfaku.korro.util.ConfigOptions ConfigOptions]].
   */
  implicit def configOptions(config: Config): ConfigOptions = new ConfigOptions(config)
}
