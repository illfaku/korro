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
package com.github.illfaku.korro.util

import java.io.{ByteArrayOutputStream, InputStream, OutputStream}
import java.nio.charset.Charset

/**
 * IO utilities.
 */
package object io {

  private [io] val EOF = -1

  private [io] val DefaultBufferSize = 4096

  private [io] val DefaultCharset = Charset.defaultCharset.name

  /**
   * Copies bytes from one stream to another.
   *
   * @param input Input stream to copy from.
   * @param output Output stream to copy to.
   * @return Number of copied bytes.
   */
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

  /**
   * Reads all bytes from input stream.
   *
   * @param input Input stream to read.
   * @return Bytes from stream.
   */
  def readBytes(input: InputStream): Array[Byte] = {
    val output = new ByteArrayOutputStream(DefaultBufferSize)
    copy(input, output)
    output.toByteArray
  }

  /**
   * Reads string from input stream.
   *
   * @param input Input stream to read.
   * @param charset Charset for string encoding.
   * @return String from stream.
   */
  def readString(input: InputStream, charset: String = DefaultCharset): String = new String(readBytes(input), charset)
}
