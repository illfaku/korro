package io.cafebabe.korro.netty

import io.cafebabe.korro.api.http.{FileRawHttpContent, MemoryRawHttpContent, RawHttpContent}
import io.cafebabe.korro.netty.ByteBufUtils.toBytes

import io.netty.buffer.{ByteBuf, CompositeByteBuf, Unpooled}

import java.nio.channels.ByteChannel
import java.nio.file.{Files, Path, StandardOpenOption}

/**
  * TODO: Add description.
  *
  * @author Vladimir Konstantinov
  */
class RawHttpContentFactory(minSize: Long, tempDir: Path) {

  private var composite: CompositeByteBuf = null

  private var file: Path = null

  private var channel: ByteChannel = null

  private var curSize = 0

  def append(buf: ByteBuf): Unit = {
    if (curSize + buf.readableBytes <= minSize) {
      if (composite == null) composite = Unpooled.compositeBuffer
      composite.addComponent(buf)
    } else {
      if (channel == null) {
        file = Files.createTempFile(tempDir, "raw-http-content-", null)
        file.toFile.deleteOnExit()
        channel = Files.newByteChannel(file, StandardOpenOption.WRITE, StandardOpenOption.APPEND)
      }
      if (composite != null) {
        composite.nioBuffers foreach channel.write
        composite.release()
        composite = null
      }
      buf.nioBuffers foreach channel.write
    }
    curSize = curSize + buf.readableBytes
  }

  def build(contentType: String): RawHttpContent = {
    if (channel != null) channel.close()
    if (file != null) {
      FileRawHttpContent(contentType, file)
    } else {
      val bytes = toBytes(composite)
      composite.release()
      MemoryRawHttpContent(contentType, bytes)
    }
  }
}
