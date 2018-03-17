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
package com.github.illfaku.korro.util.reflect

import org.scalatest._

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class ClassInstantiatorSpec extends FlatSpec with Matchers {

  import ClassInstantiator._
  import ClassInstantiatorTestData._

  "Instantiator" should "instantiate class with default values" in {
    instanceOf[TestCaseClass] should be (TestCaseClass(null, 0, flag = false))
  }

  it should "use default constructor if possible" in {
    instanceOf[SeveralConstructors].name should be ("default")
  }

  it should "throw IllegalArgumentException if there are no public constructors" in {
    a [IllegalArgumentException] should be thrownBy {
      instanceOf[PrivateConstructors]
    }
  }
}

object ClassInstantiatorTestData {

  case class TestCaseClass(name: String, value: Short, flag: Boolean)

  class PrivateConstructors private (name: String)

  class SeveralConstructors(val name: String) {
    def this(value: Integer) = this(value.toString)
    def this() = this("default")
  }
}
