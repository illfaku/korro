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
