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
package org.oxydev.korro.util.concurrent.actor

import akka.actor._

import scala.collection.mutable
import scala.concurrent.duration._

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
trait ActorDependency extends Actor {

  import context.dispatcher

  private val deps = mutable.Set.empty[DepInfo]

  def dependency(path: String)(whenResolved: ActorRef => Unit): Unit = {
    val sel = context.actorSelection(path)
    val task = scheduleIdentification(path, sel)
    deps += new DepInfo(path, sel, whenResolved, task)
  }

  private def scheduleIdentification(id: Any, selection: ActorSelection): Cancellable = {
    context.system.scheduler.schedule(Duration.Zero, 2 seconds)(selection ! Identify(id))
  }

  override def unhandled(message: Any): Unit = message match {

    case ActorIdentity(id, Some(ref)) =>
      deps.find(d => d.id == id && d.ref.isEmpty) match {
        case Some(dep) =>
          dep.task.cancel()
          context watch ref
          dep.op(ref)
          dep.ref = Some(ref)
        case None => super.unhandled(message)
      }

    case Terminated(actor) =>
      deps.find(_.ref.contains(actor)) match {
        case Some(dep) =>
          dep.ref = None
          dep.task = scheduleIdentification(dep.id, dep.sel)
        case None => super.unhandled(message)
      }

    case _ => super.unhandled(message)
  }

  override def postStop(): Unit = {
    deps foreach { dep =>
      dep.task.cancel()
      dep.ref foreach context.unwatch
    }
    deps.clear()
    super.postStop()
  }
}

private [actor] class DepInfo(val id: Any, val sel: ActorSelection, val op: ActorRef => Unit, var task: Cancellable) {

  var ref: Option[ActorRef] = None

  override val hashCode: Int = id.hashCode

  override def equals(obj: scala.Any): Boolean = obj match {
    case that: DepInfo => this.id == that.id
    case _ => false
  }
}
