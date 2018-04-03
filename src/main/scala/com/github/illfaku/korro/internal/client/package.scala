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
package com.github.illfaku.korro.internal

import java.net.URL

package object client {

  private[client] def getPort(url: URL): Int = {
    if (url.getPort == -1) {
      if (isSsl(url)) 443 else 80
    } else {
      url.getPort
    }
  }

  private[client] def isSsl(url: URL): Boolean = {
    url.getProtocol.equalsIgnoreCase("https") || url.getProtocol.equalsIgnoreCase("wss")
  }
}
