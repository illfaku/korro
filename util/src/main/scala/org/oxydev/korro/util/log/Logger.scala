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
package org.oxydev.korro.util.log

import akka.event.LoggingAdapter
import org.slf4j.LoggerFactory.getLogger

/**
 * Logger factory.
 *
 * @author Vladimir Konstantinov
 */
object Logger {
  type Logger = LoggingAdapter with TraceLoggingAdapter

  def apply(clazz: Class[_]): Logger = new Slf4jLoggingAdapter(getLogger(clazz))
  def apply(name: String): Logger = new Slf4jLoggingAdapter(getLogger(name))
}
