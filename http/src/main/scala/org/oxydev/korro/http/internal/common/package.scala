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

import io.netty.buffer.{Unpooled, ByteBuf}
import io.netty.channel.{ChannelFuture, ChannelFutureListener}

/**
 * Common utilities and channel handlers used in both server and client implementations.
 */
package object common {

  /**
   * Additional methods for [[ChannelFuture]] that make work with it a bit easier and more pretty.
   *
   * @param future Original [[ChannelFuture]] object.
   */
  implicit class ChannelFutureExt(future: ChannelFuture) {

    /**
     * Adds listener to this future to execute provided function when the future completes.
     *
     * @param op Function to execute.
     */
    def foreach(op: ChannelFuture => Unit): Unit = future.addListener(new ChannelFutureListener {
      override def operationComplete(f: ChannelFuture): Unit = op(f)
    })

    /**
     * Adds listener to this future to execute provided function when the future successfully completes.
     *
     * @param op Function to execute.
     */
    def onSuccess(op: ChannelFuture => Unit): Unit = foreach(f => if (f.isSuccess) op(f))

    /**
     * Adds listener to this future to execute provided function when the future unsuccessfully completes.
     *
     * @param op Function to execute.
     */
    def onFailure(op: ChannelFuture => Unit): Unit = foreach(f => if (!f.isSuccess) op(f))

    /**
     * Closes channel associated with this future when the future completes.
     */
    def closeChannel(): Unit = future.addListener(ChannelFutureListener.CLOSE)
  }

  /**
   * Extracts bytes from [[ByteBuf]] as byte array.
   *
   * @param buf ByteBuf to extract bytes from.
   * @return Bytes extracted from ByteBuf.
   */
  implicit def toBytes(buf: ByteBuf): Array[Byte] = {
    if (buf.isReadable) {
      val bytes = new Array[Byte](buf.readableBytes)
      buf.getBytes(0, bytes)
      bytes
    } else {
      Array.emptyByteArray
    }
  }

  /**
   * Wraps provided byte array with unpooled byte buf.
   *
   * @param bytes Byte array to wrap.
   * @return Unpooled wrapped byte buf.
   */
  implicit def toByteBuf(bytes: Array[Byte]): ByteBuf = Unpooled.wrappedBuffer(bytes)
}
