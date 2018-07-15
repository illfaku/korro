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
package io.cafebabe.korro.util.concurrent

import java.util.concurrent.ThreadFactory

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class IncrementalThreadFactory(baseName: String) extends ThreadFactory {

  private var counter = -1

  override def newThread(r: Runnable): Thread = {
    counter += 1
    new Thread(r, s"$baseName-$counter")
  }
}
