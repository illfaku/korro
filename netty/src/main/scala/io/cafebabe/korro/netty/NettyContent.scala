package io.cafebabe.korro.netty

import io.cafebabe.korro.util.protocol.http.MimeType
import io.cafebabe.korro.util.protocol.http.MimeType.Names.OctetStream

import io.netty.buffer.ByteBuf
import io.netty.channel.{DefaultFileRegion, FileRegion}

import java.io.RandomAccessFile
import java.nio.file.Path

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
trait NettyContent {
  type Data
  def data: Data
  def contentType: String
  def contentLength: Long
}

class DefaultNettyContent(buf: ByteBuf, cType: String) extends NettyContent {
  type Data = ByteBuf
  override val data: ByteBuf = buf
  override val contentType: String = cType
  override val contentLength: Long = buf.readableBytes
}

class FileStreamNettyContent(val path: Path, val position: Long) extends NettyContent {

  type Data = RandomAccessFile

  private lazy val raf = new RandomAccessFile(path.toFile, "r")

  override def data: RandomAccessFile = raf
  override def contentType: String = MimeType.Mapping.getMimeType(path).getOrElse(OctetStream)
  override def contentLength: Long = raf.length

  def toFileRegion: FileRegion = new DefaultFileRegion(data.getChannel, position, contentLength)
}
