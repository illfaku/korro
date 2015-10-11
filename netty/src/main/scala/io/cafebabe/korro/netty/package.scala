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
package io.cafebabe.korro

import io.netty.channel.{ChannelFuture, ChannelFutureListener}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
package object netty {

  implicit class ChannelFutureExt(future: ChannelFuture) {
    def foreach(f: ChannelFuture => Unit): Unit = future.addListener(new ChannelFutureListener {
      override def operationComplete(cf: ChannelFuture): Unit = f(cf)
    })
  }
}
