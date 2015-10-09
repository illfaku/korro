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
package io.cafebabe.korro.util.i18n

import org.scalatest._

import java.util.Locale

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class LocalesSpec extends FlatSpec with Matchers {

  "Locale parser" should "parse only language string" in {
    Locales.parse("lang") should be (new Locale("lang"))
  }

  it should "parse language and country string with underscore as separator" in {
    Locales.parse("lang_CNTRY") should be (new Locale("lang", "CNTRY"))
  }

  it should "parse ignoring case" in {
    Locales.parse("LANG_CNTRY") should be (new Locale("lang", "cntry"))
  }

  it should "parse HTTP header Accept-Language using first language" in {
    Locales.parse("en-ca,en;q=0.8,en-us;q=0.6,de-de;q=0.4,de;q=0.2") should be (new Locale("en", "ca"))
  }

  it should "return default locale if string is not matched by pattern" in {
    Locales.parse("4ar124") should be (Locale.getDefault)
  }

  it should "return default locale for null string" in {
    Locales.parse(null) should be (Locale.getDefault)
  }
}
