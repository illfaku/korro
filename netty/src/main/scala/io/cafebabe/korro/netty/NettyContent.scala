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
package io.cafebabe.korro.netty

import io.cafebabe.korro.util.protocol.http.MimeType
import io.cafebabe.korro.util.protocol.http.MimeType.Names.OctetStream

import io.netty.buffer.ByteBuf
import io.netty.channel.{DefaultFileRegion, FileRegion}

import java.io.RandomAccessFile
import java.nio.file.Path

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
sealed trait NettyContent {
  type Data
  def data: Data
  def contentType: String
  def contentLength: Long
}

class DefaultNettyContent(buf: ByteBuf, cType: String) extends NettyContent {
  type Data = ByteBuf
  override val data: ByteBuf = buf
  override val contentType: String = cType
  override val contentLength: Long = buf.readableBytes
}

class FileStreamNettyContent(val path: Path, val position: Long) extends NettyContent {

  type Data = RandomAccessFile

  private lazy val raf = new RandomAccessFile(path.toFile, "r")

  override def data: RandomAccessFile = raf
  override def contentType: String = MimeType.Mapping.getMimeType(path).getOrElse(OctetStream)
  override def contentLength: Long = raf.length

  def toFileRegion: FileRegion = new DefaultFileRegion(data.getChannel, position, contentLength)
}
