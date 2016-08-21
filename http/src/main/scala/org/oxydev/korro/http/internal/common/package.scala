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
package org.oxydev.korro.http.internal

import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.channel.{Channel, ChannelFuture, ChannelFutureListener}

import scala.util.{Failure, Success, Try}

/**
 * Common utilities and channel handlers used in both server and client implementations.
 */
package object common {

  /**
   * Additional methods for [[ChannelFuture]] which make work with it a bit easier and more pretty.
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
     * Adds listener to this future to execute provided function when the future completes.
     *
     * @param op Function to execute.
     */
    def onComplete(op: Try[Channel] => Unit): Unit = foreach { f =>
      val t = if (f.isSuccess) Success(f.channel) else Failure(f.cause)
      op(t)
    }

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
