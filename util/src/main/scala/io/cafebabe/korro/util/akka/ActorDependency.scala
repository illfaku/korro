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
package io.cafebabe.korro.util.akka

import akka.actor._

import scala.concurrent.duration._
import scala.util.control.NoStackTrace

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
trait ActorDependency extends Actor {

  import context.dispatcher
  private val scheduler = context.system.scheduler

  private var sel: Option[ActorSelection] = None
  private var op: Option[(ActorRef => Unit)] = None

  private var dep: Option[ActorRef] = None

  private var task: Option[Cancellable] = None

  def dependency(path: String)(whenResolved: ActorRef => Unit): Unit = {
    sel = Option(path).map(context.actorSelection)
    op = Option(whenResolved)
    scheduleIdentification()
  }

  private def scheduleIdentification(): Unit = {
    task = sel map { selection => scheduler.schedule(Duration.Zero, 2 seconds)(selection ! Identify('dep)) }
  }

  override def unhandled(message: Any): Unit = message match {
    case ActorIdentity('dep, Some(ref)) if dep.isEmpty =>
      task.foreach(_.cancel())
      dep = Some(ref)
      context watch ref
      op.foreach(_(ref))
    case Terminated(actor) if dep contains actor =>
      dep = None
      scheduleIdentification()
    case _ => super.unhandled(message)
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    dep foreach context.unwatch
    super.preRestart(reason, message)
  }

  override def postStop(): Unit = {
    task.foreach(_.cancel())
    super.postStop()
  }
}
