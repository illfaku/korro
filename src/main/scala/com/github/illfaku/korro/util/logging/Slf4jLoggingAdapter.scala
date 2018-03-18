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
package com.github.illfaku.korro.util.logging

import akka.event.LoggingAdapter

/**
 * slf4j logging adapter with more convenient methods presented by
 * <a href="http://doc.akka.io/api/akka/2.4.2/#akka.event.LoggingAdapter">`akka.event.LoggingAdapter`</a>.
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
