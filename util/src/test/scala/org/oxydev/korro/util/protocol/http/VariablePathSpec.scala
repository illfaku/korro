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
package org.oxydev.korro.util.protocol.http

import org.scalatest.{FlatSpec, Matchers}

/**
 * Collection of tests for [[VariablePath VariablePath]].
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
