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

import akka.actor.{Status, Actor, Props}

import java.nio.file.{Path, Paths}

import scala.util.control.NoStackTrace

/**
  * TODO: Add description.
  *
  * @author Vladimir Konstantinov
  */
object TempFileStorageActor {

  def props(limit: Long): Props = {
    val dir = Paths get sys.props("java.io.tmpdir")
    props(dir, limit)
  }

  def props(dir: Path, limit: Long): Props = {
    Props(new TempFileStorageActor(dir, limit))
  }

  // DTO
  case class NewTempFile(size: Long)
  case object NoFreeSpaceException extends Exception with NoStackTrace

  // internal
  private [temp] case class UpdateSize(size: Long)
}

class TempFileStorageActor(dir: Path, limit: Long) extends Actor {

  private var storageSize = 0L

  override def preStart(): Unit = {
    super.preStart()
  }

  import TempFileStorageActor._

  override def receive = {

    case NewTempFile(size) if storageSize + size <= limit =>
      sender ! context.actorOf(TempFileActor.props(dir))

    case NewTempFile(_) => sender ! Status.Failure(NoFreeSpaceException)
  }

  override def postStop(): Unit = {
    super.postStop()
  }
}
