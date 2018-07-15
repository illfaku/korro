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
package io.cafebabe.korro.util.protocol.http

import org.scalatest.{FlatSpec, Matchers}

/**
 * Collection of tests for [[io.cafebabe.korro.util.protocol.http.VariablePath VariablePath]].
 *
 * @author Vladimir Konstantinov
 */
class VariablePathSpec extends FlatSpec with Matchers {

  "VariablePath" should "extract no variables if pattern is equal to path" in {
    object NoVariablePath extends VariablePath("/test/path")
    NoVariablePath.unapplySeq("/test/path") should be (Some(Nil))
  }

  it should "extract all variables from path" in {
    object TestVariablePath extends VariablePath("/test/{}/path/{}")
    TestVariablePath.unapplySeq("/test/param/path/101") should be (Some(List("param", "101")))
  }

  it should "extract nothing if path does not match pattern" in {
    object TestVariablePath extends VariablePath("/test/{}/path/{}")
    TestVariablePath.unapplySeq("/test/param/path") should be (None)
  }

  it should "extract nothing if path is null" in {
    object TestVariablePath extends VariablePath("/test/{}/path/{}")
    TestVariablePath.unapplySeq(null) should be (None)
  }
}
