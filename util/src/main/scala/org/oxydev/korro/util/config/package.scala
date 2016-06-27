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
package org.oxydev.korro.util

import com.typesafe.config.Config

/**
 * Utilities for <a href="http://typesafehub.github.io/config/">`config`</a> library.
 */
package object config {

  /**
   * Converts <a href="http://typesafehub.github.io/config/latest/api/com/typesafe/config/Config.html">`Config`</a>
   * object to [[org.oxydev.korro.util.config.ConfigExt ConfigExt]].
   */
  implicit def extended(config: Config): ConfigExt = new ConfigExt(config)
}
