package io.cafebabe.korro.util

/**
  * TODO: Add description.
  *
  * @author Vladimir Konstantinov
  */
package object lang {

  object & {
    def unapply[A](a: A) = Some(a, a)
  }
}
