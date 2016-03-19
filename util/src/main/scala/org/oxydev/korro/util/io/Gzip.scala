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
package org.oxydev.korro.util.io

import org.oxydev.korro.util.lang.Loan._

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream}
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

/**
 * Utility methods for compression/decompression of bytes and strings using
 * <a href="http://docs.oracle.com/javase/8/docs/api/java/util/zip/GZIPInputStream.html">`GZIPInputStream`</a> and
 * <a href="http://docs.oracle.com/javase/8/docs/api/java/util/zip/GZIPOutputStream.html">`GZIPOutputStream`</a>.
 */
object Gzip {

  /**
   * Compresses bytes.
   */
  def zip(bytes: Array[Byte]): Array[Byte] = {
    val output = new ByteArrayOutputStream(DefaultBufferSize)
    loan (new GZIPOutputStream(output)) to (_.write(bytes))
    output.toByteArray
  }

  /**
   * Compresses string.
   */
  def zipString(s: String, charset: String = DefaultCharset): Array[Byte] = zip(s.getBytes(charset))

  /**
   * Decompresses input stream.
   */
  def unzip(input: InputStream): Array[Byte] = loan (new GZIPInputStream(input)) to readBytes

  /**
   * Decompresses bytes.
   */
  def unzip(bytes: Array[Byte]): Array[Byte] = unzip(new ByteArrayInputStream(bytes))

  /**
   * Decompresses string.
   */
  def unzipString(input: InputStream, charset: String = DefaultCharset): String = new String(unzip(input), charset)
}
