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
package org.oxydev.korro.util.io.file.temp

import org.oxydev.korro.util.io.file.temp.TempFileStorageActor.UpdateSize

import akka.actor.{Actor, Props}

import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel
import java.nio.file.{Files, Path, StandardOpenOption}

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
