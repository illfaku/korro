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
 * Mixin for <a href="http://doc.akka.io/api/akka/2.4.2/#akka.actor.Actor">`Actor`</a> that adds functionality
 * to be dependent on another actors knowing only their paths.
 *
 * {{{
 *   class MyActor extends Actor with ActorDependency {
 *     dependency("/user/my-another-actor")(_ ! Listen(self))
 *     override def receive = ???
 *   }
 * }}}
 *
 * When dependency is added this trait schedules <a href="http://doc.akka.io/api/akka/2.4.2/#akka.actor.Identify">
 * `Identify`</a> message to be sent to it every 1 second until
 * <a href="http://doc.akka.io/api/akka/2.4.2/#akka.actor.ActorIdentity">`ActorIdentity`</a> is received, then it
 * executes `whenResolved` callback with resolved `ActorRef`. Also this trait starts watching resolved actor and
 * when/if it terminates executes `whenTerminated` callback and schedules `Identify` messages to it again.
 *
 * This trait is not using other threads to do its logic (except for scheduling) so you can safely use actor's context
 * inside of `whenResolved` and `whenTerminated` callbacks.
 *
 * {{{
 *   class MyActor extends Actor with ActorDependency {
 *     dependency("/user/my-another-actor")(context become initialized(_), context become uninitialized)
 *     def uninitialized: Receive = ???
 *     def initialized(ref: ActorRef): Receive = ???
 *     override def receive: Receive = uninitialized
 *   }
 * }}}
 *
 * Note: this trait overrides `unhandled` and `postStop` methods of `Actor` trait, so be sure to call their super
 * implementations if you override them too.
 */
trait ActorDependency extends Actor {

  import context.dispatcher

  private val deps = mutable.Set.empty[DepInfo]

  /**
   * Adds dependency to actor. This method can be invoked several times to add multiple dependencies.
   *
   * @param path Path to actor.
   * @param whenResolved Operation to execute when actor is resolved.
   * @param whenTerminated Optional operation to execute when actor is terminated.
   */
  protected def dependency(path: String)(whenResolved: ActorRef => Unit, whenTerminated: => Unit = ()): Unit = {
    val sel = context.actorSelection(path)
    val task = scheduleIdentification(path, sel)
    deps += new DepInfo(path, sel, whenResolved, () => whenTerminated, task)
  }

  private def scheduleIdentification(id: Any, selection: ActorSelection): Cancellable = {
    context.system.scheduler.schedule(Duration.Zero, 1 seconds)(selection ! Identify(id))
  }

  override def unhandled(message: Any): Unit = message match {

    case ActorIdentity(id, Some(ref)) =>
      deps.find(d => d.id == id && d.ref.isEmpty) match {
        case Some(dep) =>
          dep.task.cancel()
          context watch ref
          dep.wr(ref)
          dep.ref = Some(ref)
        case None => super.unhandled(message)
      }

    case Terminated(actor) =>
      deps.find(_.ref.contains(actor)) match {
        case Some(dep) =>
          dep.wt()
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

private [actor] class DepInfo(
  val id: Any, val sel: ActorSelection, val wr: ActorRef => Unit, val wt: () => Unit, var task: Cancellable
) {
  var ref: Option[ActorRef] = None
  override val hashCode: Int = id.hashCode
  override def equals(obj: scala.Any): Boolean = obj match {
    case that: DepInfo => this.id == that.id
    case _ => false
  }
}
