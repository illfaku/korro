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

import io.cafebabe.korro.util.log.Logger
import io.cafebabe.korro.util.lang.Loan._

import java.io.{Reader, InputStreamReader}
import java.nio.file.{Paths, Files}

import scala.collection.JavaConversions._

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object MimeType {

  object Names {
    val TextPlain = "text/plain"
    val ApplicationJson = "application/json"
    val FormUrlEncoded = "application/x-www-form-urlencoded"
  }

  object Mapping {

    private val log = Logger(getClass)

    private var mime2Ext: Map[String, List[String]] = null

    private var ext2mime: Map[String, List[String]] = null

    private def getMime2Ext: Map[String, List[String]] = {
      if (mime2Ext == null) {
        try {
          val lines = Files.readAllLines(Paths.get(getClass.getClassLoader.getResource("/mime.types").toURI))
          mime2Ext = lines.map(_.split(" ", 2)).map(l => l(0) -> l(1).split(" ").toList).toMap
        } catch {
          case e: Throwable => log.error("Failed to load mime types.")
        }
      }
      mime2Ext
    }

    private def getExt2Mime: Map[String, List[String]] = {
      if (ext2mime == null) {
        ext2mime = getMime2Ext.toList.flatMap(e => e._2.map(_ -> e._1)).groupBy(_._1).mapValues(_.map(_._2))
      }
      ext2mime
    }

    def getExtension(mimeType: String): Option[String] = getMime2Ext.get(mimeType).map(_.head)

    def getMimeType(extension: String): Option[String] = getExt2Mime.get(extension).map(_.head)
  }
}
