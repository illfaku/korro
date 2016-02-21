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
package org.oxydev.korro.util.i18n

import java.util.Locale

/**
 * Locale utilities.
 *
 * @author Vladimir Konstantinov
 */
object Locales {

  private val Pattern = """^([a-zA-Z]{1,8})(?:[_-]([a-zA-Z]{1,8}))?""".r.unanchored

  /**
   * Parses locale string to produce instance of [[java.util.Locale Locale]].
   * <p>Locale string should match the following pattern:
   * {{{
   *   ^([a-zA-Z]{1,8})(?:[_-]([a-zA-Z]{1,8}))?
   * }}}
   *
   * @param locale String representation of locale.
   * @return New instance of Locale or default Locale if string is null or is not matched by any pattern.
   */
  def parse(locale: String): Locale = locale match {
    case Pattern(language, country) => new Locale(language, Option(country).getOrElse(""))
    case _ => Locale.getDefault
  }
}
