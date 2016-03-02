/*
 * Copyright (C) 2015, 2016  Vladimir Konstantinov, Yuriy Gintsyak
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
