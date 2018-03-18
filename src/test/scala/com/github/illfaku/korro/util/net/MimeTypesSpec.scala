/*
 * Copyright 2018 Vladimir Konstantinov
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
package com.github.illfaku.korro.util.net

import com.github.illfaku.korro.util.MimeTypes

import org.scalatest.{FlatSpec, Matchers}

class MimeTypesSpec extends FlatSpec with Matchers {

  "MimeTypes" should "return application/octet-stream for empty string" in {
    MimeTypes("") should be ("application/octet-stream")
  }

  it should "return application/octet-stream for unknown extension" in {
    MimeTypes("qwerty") should be ("application/octet-stream")
  }

  it should "return text/plain for txt string" in {
    MimeTypes("txt") should be ("text/plain")
  }

  it should "return application/json for /home/data.json string" in {
    MimeTypes("/home/data.json") should be ("application/json")
  }
}
