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
package org.oxydev.korro.util.net

import org.oxydev.korro.util.lang.Loan.loan
import org.oxydev.korro.util.log.Logging

import java.io.{BufferedReader, InputStreamReader}

import scala.collection.mutable.ListBuffer

/**
 * MIME types lookup.
 */
object MimeTypes extends Logging {

  private lazy val map: Map[String, String] =
    try {
      loan (getClass.getClassLoader.getResource("/META-INF/mime.types").openStream()) to { in =>
        val reader = new BufferedReader(new InputStreamReader(in))
        val result = ListBuffer.empty[(String, String)]
        var line = reader.readLine()
        while (line != null) {
          if (!line.startsWith("#")) {
            val parts = line.split("""\s+""")
            val mime = parts.head
            parts.tail foreach (e => result += (e -> mime))
          }
          line = reader.readLine()
        }
        result.toMap
      }
    } catch {
      case e: Throwable =>
        log.error(e, "Failed to load mime types.")
        Map.empty
    }

  /**
   * Returns MIME type mapped to provided file name or extension.
   * Or `application/octet-stream` if corresponding MIME type is not found.
   */
  def apply(s: String): String = {
    val pos = s.lastIndexOf('.')
    val ext = if (pos >= 0) s.substring(pos + 1) else s
    map.getOrElse(ext.toLowerCase, "application/octet-stream")
  }
}
