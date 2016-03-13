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

import akka.actor.{Actor, Props, Status}

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
