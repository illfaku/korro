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
package org.oxydev.korro.util.log

import akka.event.LoggingAdapter

/**
 * Mixin for <a href="http://doc.akka.io/api/akka/2.4.2/#akka.event.LoggingAdapter">`akka.event.LoggingAdapter`</a>
 * with methods for trace logging.
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
