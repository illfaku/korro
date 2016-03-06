/*
 * Copyright (C) 2015, 2016  Vladimir Konstantinov, Yuriy Gintsyak
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
package org.oxydev.korro.http.internal

import io.netty.channel.{ChannelFuture, ChannelFutureListener}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
package object common {

  implicit class ChannelFutureExt(future: ChannelFuture) {

    def foreach(f: ChannelFuture => Unit): Unit = future.addListener(new ChannelFutureListener {
      override def operationComplete(future: ChannelFuture): Unit = f(future)
    })

    def onSuccess(f: ChannelFuture => Unit): Unit = if (future.isSuccess) foreach(f)

    def onFailure(f: ChannelFuture => Unit): Unit = if (!future.isSuccess) foreach(f)

    def closeChannel(): Unit = future.addListener(ChannelFutureListener.CLOSE)
  }
}
