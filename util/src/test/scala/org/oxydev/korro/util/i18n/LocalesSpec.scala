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
package org.oxydev.korro.util.i18n

import org.scalatest._

import java.util.Locale

/**
 * Tests for [[org.oxydev.korro.util.i18n.Locales Locales]] object.
 */
class LocalesSpec extends FlatSpec with Matchers {

  "Locale parser" should "successfully parse string if only language part is present" in {
    Locales.parse("lang") should be (new Locale("lang"))
  }

  it should "successfully parse string if language and country parts are present with underscore as separator" in {
    Locales.parse("lang_CNTRY") should be (new Locale("lang", "CNTRY"))
  }

  it should "successfully parse string as in Accept-Language header (RFC2616) using first language" in {
    Locales.parse("en-ca,en;q=0.8,en-us;q=0.6,de-de;q=0.4,de;q=0.2") should be (new Locale("en", "ca"))
  }

  it should "return default locale if string is not matched by pattern" in {
    Locales.parse("4ar124") should be (Locale.getDefault)
  }

  it should "return default locale for null" in {
    Locales.parse(null) should be (Locale.getDefault)
  }
}
