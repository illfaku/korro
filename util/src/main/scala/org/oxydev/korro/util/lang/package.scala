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
package org.oxydev.korro.util

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * TODO: Add description.
  *
  * @author Vladimir Konstantinov
  */
package object lang {

  object & {
    def unapply[A](a: A) = Some(a, a)
  }

  implicit def either2try[F <: Throwable, R](either: Either[F, R]): Try[R] = either match {
    case Right(r) => Success(r)
    case Left(f) => Failure(f)
  }

  implicit def either2future[F <: Throwable, R](either: Either[F, R]): Future[R] = Future fromTry either2try(either)
}
