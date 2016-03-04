/*
 * Copyright (C) 2015  Vladimir Konstantinov, Yuriy Gintsyak
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
