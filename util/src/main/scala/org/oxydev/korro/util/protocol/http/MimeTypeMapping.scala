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

import org.oxydev.korro.util.lang.Loan.loan
import org.oxydev.korro.util.log.Logging

import java.io.{BufferedReader, InputStreamReader}

import scala.collection.mutable.ListBuffer

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object MimeTypeMapping extends Logging {

  private lazy val mime2Ext: Map[String, List[String]] = {
    try {
      loan (getClass.getClassLoader.getResource("/mime.types").openStream()) to { in =>
        val reader = new BufferedReader(new InputStreamReader(in))
        val result = ListBuffer.empty[(String, List[String])]
        var line = reader.readLine()
        while (line != null) {
          val parts = line.split("""\s+""")
          if (parts.length > 1) result += (parts.head -> parts.tail.toList)
          line = reader.readLine()
        }
        result.toMap
      }
    } catch {
      case e: Throwable =>
        log.error(e, "Failed to load mime types.")
        Map.empty
    }
  }

  private lazy val ext2mime: Map[String, List[String]] = {
    mime2Ext.toList.flatMap(e => e._2.map(_ -> e._1)).groupBy(_._1).mapValues(_.map(_._2))
  }

  def getExtension(mimeType: String): Option[String] = mime2Ext.get(mimeType.toLowerCase).map(_.head)

  def getMimeType(s: String): Option[String] = {
    val pos = s.lastIndexOf('.')
    if (pos >= 0) ext2mime.get(s.substring(pos + 1).toLowerCase).map(_.head)
    else ext2mime.get(s.toLowerCase).map(_.head)
  }
}
