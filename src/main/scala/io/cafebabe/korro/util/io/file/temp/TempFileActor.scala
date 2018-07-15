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
package io.cafebabe.korro.util.io.file.temp

import io.cafebabe.korro.util.io.file.temp.TempFileStorageActor.UpdateSize

import akka.actor.{Props, Actor}

import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel
import java.nio.file.{StandardOpenOption, Files, Path}

/**
  * TODO: Add description.
  *
  * @author Vladimir Konstantinov
  */
object TempFileActor {

  def props(dir: Path): Props = Props(new TempFileActor(dir))

  // DTO
  case class Append(buf: ByteBuffer, last: Boolean = false)
}

class TempFileActor(dir: Path) extends Actor {

  private var path: Path = null

  private var ch: SeekableByteChannel = null

  private var size = 0L

  private def file: Path = {
    if (path == null) {
      path = Files.createTempFile(dir, "temp-file-", "")
      path.toFile.deleteOnExit()
    }
    path
  }

  private def channel: SeekableByteChannel = {
    if (ch == null) ch = Files.newByteChannel(file, StandardOpenOption.WRITE, StandardOpenOption.APPEND)
    ch
  }

  import TempFileActor._

  override def receive = {
    case Append(buf, last) =>
      buf.flip()
      size = size + buf.limit
      channel.write(buf)
      if (last) {
        closeChannel()
        context.parent ! UpdateSize(size)
      }
  }

  private def closeChannel(): Unit = if (ch != null) {
    ch.close()
    ch == null
  }

  override def postStop(): Unit = {
    closeChannel()
    if (path != null) path.toFile.delete()
    super.postStop()
  }
}
