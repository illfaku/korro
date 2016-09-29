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

import akka.actor.ActorRef

/**
 * Marker trait for all route commands.
 */
sealed trait RouteCommand

/**
 * Command for router to set your actor as handler of matched requests.
 *
 * @param ref Actor reference to set.
 * @param predicate Predicate to test requests against.
 * @param instructions Set of instructions for request handling.
 */
case class SetRoute(ref: ActorRef, predicate: RoutePredicate, instructions: Set[RouteInstruction]) extends RouteCommand

/**
 * Command for router to remove your actor from handlers list.
 *
 * <p>It is not necessary to send this command, because when your actor terminates corresponding route will be
 * automatically unset.
 *
 * @param ref Actor reference to unset.
 */
case class UnsetRoute(ref: ActorRef) extends RouteCommand
