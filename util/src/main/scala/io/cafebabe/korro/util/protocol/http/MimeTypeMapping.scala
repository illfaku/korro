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
package io.cafebabe.korro.util.protocol.http

import io.cafebabe.korro.util.lang.Loan.loan
import io.cafebabe.korro.util.log.Logging

import java.io.{BufferedReader, InputStreamReader}
import java.nio.file.Path
import java.util.stream.Collectors

import scala.collection.JavaConverters._

object MimeTypeMapping extends Logging {

  private lazy val mime2Ext: Map[String, List[String]] = {
    try {
      loan (getClass.getClassLoader.getResource("/mime.types").openStream()) to { in =>
        val reader = new BufferedReader(new InputStreamReader(in))
        reader.lines.collect(Collectors.toList[String]).asScala.toStream
          .map(_.split(" ", 2)).filter(_.length == 2)
          .map(l => l(0) -> l(1).split(" ").toList).toMap
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

  def getMimeType(extension: String): Option[String] = ext2mime.get(extension.toLowerCase).map(_.head)

  def getMimeType(path: Path): Option[String] = {
    val filename = path.toFile.getName
    filename.lastIndexOf('.') match {
      case -1 => None
      case pos =>
        val extension = filename.substring(pos + 1)
        getMimeType(extension)
    }
  }
}
