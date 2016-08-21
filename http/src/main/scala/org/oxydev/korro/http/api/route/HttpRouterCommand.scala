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
package org.oxydev.korro.http.api.route

import org.oxydev.korro.http.api.HttpRequest
import org.oxydev.korro.util.lang.Predicate1

import akka.actor.ActorRef

/**
 * Common trait for all router commands.
 */
sealed trait HttpRouterCommand

/**
 * Command for router to set your actor as handler of matched requests.
 *
 * @param ref Actor reference to set.
 * @param predicate Predicate to test requests against.
 */
case class SetRoute(ref: ActorRef, predicate: Predicate1[HttpRequest]) extends HttpRouterCommand

/**
 * Command for router to remove your actor from handlers list.
 *
 * <p>It is not necessary to send this command, because when your actor terminates corresponding route will be
 * automatically unset.
 *
 * @param ref Actor reference to unset.
 */
case class UnsetRoute(ref: ActorRef) extends HttpRouterCommand
