package io.cafebabe.korro.util.log

import io.cafebabe.korro.util.log.Logger.Logger

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
trait Logging {
  protected val log: Logger = Logger(getClass)
}
