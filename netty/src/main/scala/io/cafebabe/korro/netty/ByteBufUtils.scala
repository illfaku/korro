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

import io.netty.buffer.{ByteBuf, Unpooled}

import java.nio.charset.Charset

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object ByteBufUtils {

  implicit def toBytes(buf: ByteBuf): Array[Byte] = {
    if (buf.hasArray) {
      buf.array
    } else {
      val bytes = new Array[Byte](buf.readableBytes)
      buf.getBytes(0, bytes)
      bytes
    }
  }

  implicit def toByteBuf(bytes: Array[Byte]): ByteBuf = Unpooled.wrappedBuffer(bytes)

  def toByteBuf(text: CharSequence, charset: Charset): ByteBuf = text.toString.getBytes(charset)

  def emptyByteBuf: ByteBuf = Unpooled.buffer(0)
}
