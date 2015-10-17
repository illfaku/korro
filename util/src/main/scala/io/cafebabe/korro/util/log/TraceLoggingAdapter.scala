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
package io.cafebabe.korro.util.log

import akka.event.LoggingAdapter

/**
 * Mixin for [[akka.event.LoggingAdapter]] with methods for trace logging.
 *
 * @author Vladimir Konstantinov
 */
trait TraceLoggingAdapter { this: LoggingAdapter =>

  def isTraceEnabled: Boolean

  protected def notifyTrace(message: String): Unit

  def trace(message: String): Unit = if (isTraceEnabled) notifyTrace(message)
  def trace(template: String, arg1: Any): Unit = if (isTraceEnabled) notifyTrace(format1(template, arg1))
  def trace(template: String, arg1: Any, arg2: Any): Unit = if (isTraceEnabled) notifyTrace(format(template, arg1, arg2))
  def trace(template: String, arg1: Any, arg2: Any, arg3: Any): Unit = if (isTraceEnabled) notifyTrace(format(template, arg1, arg2, arg3))
  def trace(template: String, arg1: Any, arg2: Any, arg3: Any, arg4: Any): Unit = if (isTraceEnabled) notifyTrace(format(template, arg1, arg2, arg3, arg4))

  private def format1(t: String, arg: Any): String = arg match {
    case a: Array[_] if !a.getClass.getComponentType.isPrimitive => format(t, a: _*)
    case a: Array[_] => format(t, a.map(_.asInstanceOf[AnyRef]): _*)
    case x => format(t, x)
  }
}
