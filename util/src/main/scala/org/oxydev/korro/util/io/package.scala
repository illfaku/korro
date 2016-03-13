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
