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
 * slf4j logging adapter with more convenient methods presented by [[akka.event.LoggingAdapter]].
 *
 * @author Vladimir Konstantinov
 */
class Slf4jLoggingAdapter(logger: org.slf4j.Logger) extends LoggingAdapter with TraceLoggingAdapter {

  override def isErrorEnabled: Boolean = logger.isErrorEnabled
  override def isWarningEnabled: Boolean = logger.isWarnEnabled
  override def isInfoEnabled: Boolean = logger.isInfoEnabled
  override def isDebugEnabled: Boolean = logger.isDebugEnabled
  override def isTraceEnabled: Boolean = logger.isTraceEnabled

  override protected def notifyError(cause: Throwable, message: String): Unit = logger.error(message, cause)
  override protected def notifyError(message: String): Unit = logger.error(message)
  override protected def notifyWarning(message: String): Unit = logger.warn(message)
  override protected def notifyInfo(message: String): Unit = logger.info(message)
  override protected def notifyDebug(message: String): Unit = logger.debug(message)
  override protected def notifyTrace(message: String): Unit = logger.trace(message)
}
