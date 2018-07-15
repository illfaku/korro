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
package io.cafebabe.korro.util.reflect

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
