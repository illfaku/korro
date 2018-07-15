package io.cafebabe.korro.util

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
