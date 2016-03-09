/*
 * Copyright (C) 2015, 2016  Vladimir Konstantinov, Yuriy Gintsyak
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
package org.oxydev.korro.util.lang

object Predicate {

  def apply[A](test: A => Boolean): Predicate1[A] = new Predicate1[A] {
    override def apply(a: A): Boolean = test(a)
  }
}

trait Predicate1[A] extends (A => Boolean) { self =>

  def &&(other: Predicate1[A]): Predicate1[A] = new Predicate1[A] {
    override def apply(a: A): Boolean = self(a) && other(a)
  }

  def ||(other: Predicate1[A]): Predicate1[A] = new Predicate1[A] {
    override def apply(a: A): Boolean = self(a) || other(a)
  }

  def unary_! : Predicate1[A] = new Predicate1[A] {
    override def apply(a: A): Boolean = !self(a)
  }
}
