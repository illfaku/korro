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
package com.github.illfaku.korro.internal

import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.channel.{ChannelFuture, ChannelFutureListener}

package object common {

  implicit class ChannelFutureExt(future: ChannelFuture) {

    def foreach(op: ChannelFuture => Unit): Unit = future.addListener(op(_))

    def onSuccess(op: ChannelFuture => Unit): Unit = foreach(f => if (f.isSuccess) op(f))

    def onFailure(op: ChannelFuture => Unit): Unit = foreach(f => if (!f.isSuccess) op(f))

    def closeChannel(): Unit = future.addListener(ChannelFutureListener.CLOSE)
  }

  implicit def byteBuf2bytes(buf: ByteBuf): Array[Byte] = {
    if (buf.isReadable) {
      val bytes = new Array[Byte](buf.readableBytes)
      buf.getBytes(0, bytes)
      bytes
    } else {
      Array.emptyByteArray
    }
  }

  implicit def bytes2byteBuf(bytes: Array[Byte]): ByteBuf = Unpooled.wrappedBuffer(bytes)
}
