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
package org.oxydev.korro.util

import org.oxydev.korro.util.lang.Loan.loan

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream, OutputStream}
import java.nio.charset.Charset
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
package object io {

  private val EOF = -1

  private val DefaultBufferSize = 4096

  private val DefaultCharset = Charset.defaultCharset.name

  def copy(input: InputStream, output: OutputStream): Int = {
    val buf = new Array[Byte](DefaultBufferSize)
    var count = 0
    var n = input.read(buf)
    while (n != EOF) {
      output.write(buf, 0, n)
      count += n
      n = input.read(buf)
    }
    count
  }

  def readBytes(input: InputStream): Array[Byte] = {
    val output = new ByteArrayOutputStream(DefaultBufferSize)
    copy(input, output)
    output.toByteArray
  }

  def readString(input: InputStream, charset: String = DefaultCharset): String = new String(readBytes(input), charset)

  def zip(bytes: Array[Byte]): Array[Byte] = {
    val output = new ByteArrayOutputStream(DefaultBufferSize)
    loan (new GZIPOutputStream(output)) to (_.write(bytes))
    output.toByteArray
  }

  def zipString(s: String, charset: String = DefaultCharset): Array[Byte] = zip(s.getBytes(charset))

  def unzip(input: InputStream): Array[Byte] = loan (new GZIPInputStream(input)) to readBytes

  def unzip(bytes: Array[Byte]): Array[Byte] = unzip(new ByteArrayInputStream(bytes))

  def unzipString(input: InputStream, charset: String = DefaultCharset): String = new String(unzip(input), charset)
}
