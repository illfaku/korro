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
package org.oxydev.korro.util.akka

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
  private val scheduler = context.system.scheduler

  private val deps = mutable.Set.empty[DependencyInfo]

  def dependency(path: String)(whenResolved: ActorRef => Unit = null): Unit = {
    val sel = context.actorSelection(path)
    val op = Option(whenResolved)
    val task = scheduleIdentification(sel)
    deps += new DependencyInfo(sel, op, task, None)
  }

  private def scheduleIdentification(selection: ActorSelection): Cancellable = {
    scheduler.schedule(Duration.Zero, 2 seconds)(selection ! Identify(selection))
  }

  override def unhandled(message: Any): Unit = message match {

    case ActorIdentity(id, Some(ref)) =>
      deps.find(d => d.ref.isEmpty && d.sel == id) match {
        case Some(dep) =>
          dep.task.cancel()
          context watch ref
          dep.op foreach (_(ref))
          deps += dep.withRef(ref)
        case None => super.unhandled(message)
      }

    case Terminated(actor) =>
      deps.find(_.ref.contains(actor)) match {
        case Some(dep) =>
          val task = scheduleIdentification(dep.sel)
          deps += dep.withTask(task)
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

private [akka] class DependencyInfo(
  val sel: ActorSelection, val op: Option[(ActorRef => Unit)], val task: Cancellable, val ref: Option[ActorRef]
) {

  def withTask(t: Cancellable): DependencyInfo = new DependencyInfo(sel, op, t, None)

  def withRef(r: ActorRef): DependencyInfo = new DependencyInfo(sel, op, task, Option(r))

  override def hashCode(): Int = sel.hashCode

  override def equals(obj: scala.Any): Boolean = obj match {
    case that: DependencyInfo => this.sel.equals(that.sel)
    case _ => false
  }
}
