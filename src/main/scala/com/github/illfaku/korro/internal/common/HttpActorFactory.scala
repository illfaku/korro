/*
 * Copyright 2018 Vladimir Konstantinov
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
package com.github.illfaku.korro.internal.common

import akka.actor.{Actor, Props}
import io.netty.channel.Channel

private[internal] trait HttpActorFactory { this: Actor =>

  import HttpActorFactory._

  protected def httpActorCreation: Receive = {

    case NewHttpActor(channel) => sender ! context.actorOf(httpActorProps(channel))
  }
}

private[internal] object HttpActorFactory {

  case class NewHttpActor(channel: Channel)

  def httpActorProps(channel: Channel): Props = Props(new HttpActor(channel))
}
