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
package io.cafebabe.korro.netty

import io.netty.handler.codec.http.FullHttpRequest
import io.netty.util.ReferenceCounted

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
trait NettyRequestHolder extends ReferenceCounted {

  def req: FullHttpRequest

  override def refCnt: Int = req.refCnt
  override def retain(): ReferenceCounted = req.retain()
  override def retain(increment: Int): ReferenceCounted = req.retain(increment)
  override def release(): Boolean = req.release()
  override def release(decrement: Int): Boolean = req.release(decrement)
}
