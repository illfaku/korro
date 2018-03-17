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
package com.github.illfaku.korro.util

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * Common utilities.
  */
package object lang {

  /**
   * Concatenates 2 extractors in a case statement.
   */
  object & {
    def unapply[A](a: A) = Some(a, a)
  }

  /**
   * Converts <a href="http://www.scala-lang.org/api/current/#scala.util.Either">`Either`</a> which left type is
   * subclass of `Throwable` to <a href="http://www.scala-lang.org/api/current/#scala.util.Try">`Try`</a>.
   */
  implicit def either2try[F <: Throwable, R](either: Either[F, R]): Try[R] = either match {
    case Right(r) => Success(r)
    case Left(f) => Failure(f)
  }

  /**
   * Converts <a href="http://www.scala-lang.org/api/current/#scala.util.Either">`Either`</a> which left type is
   * subclass of `Throwable` to <a href="http://www.scala-lang.org/api/current/#scala.concurrent.Future">`Future`</a>.
   */
  implicit def either2future[F <: Throwable, R](either: Either[F, R]): Future[R] = Future fromTry either2try(either)

  /**
   * Implicitly adds method `toOption` to all types.
   */
  implicit class Optionable[T <: Any](value: T) {

    /**
     * Converts this value to `Option`.
     */
    def toOption: Option[T] = Option(value)
  }
}
