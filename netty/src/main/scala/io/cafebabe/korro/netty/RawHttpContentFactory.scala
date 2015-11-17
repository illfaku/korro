package io.cafebabe.korro.netty

import io.cafebabe.korro.api.http._
import io.cafebabe.korro.netty.ByteBufUtils.toBytes

import akka.actor._
import io.netty.buffer.{ByteBuf, CompositeByteBuf, Unpooled}

import java.nio.file.{Files, Path, StandardOpenOption}

import scala.concurrent.duration.FiniteDuration

/**
  * TODO: Add description.
  * TODO: Temporal solution.
  *
  * @author Vladimir Konstantinov
  */
class RawHttpContentFactory(minSize: Long, tempDir: Path, tempFileTtl: FiniteDuration, ctx: ActorContext) {

  private var composite: CompositeByteBuf = null

  private var file: Path = null
  private var fileActor: ActorRef = null

  private var curSize = 0

  def append(buf: ByteBuf, last: Boolean = false): Unit = {
    val bufSize = buf.readableBytes
    if (curSize + bufSize <= minSize) {
      if (composite == null) composite = Unpooled.compositeBuffer
      composite.addComponent(buf)
    } else {
      if (file == null) {
        file = Files.createTempFile(tempDir, "raw-http-content-", "")
        file.toFile.deleteOnExit()
        fileActor = ctx.actorOf(Props(classOf[TempFileActor], file, tempFileTtl), file.toFile.getName)
      }
      if (composite != null) {
        fileActor ! AppendBuf(composite, last)
        composite = null
      }
      fileActor ! AppendBuf(buf, last)
    }
    curSize = curSize + bufSize
  }

  def build(contentType: String): HttpContent = {
    if (file != null) {
      FileRawHttpContent(contentType, file)
    } else if (composite != null) {
      val bytes = toBytes(composite)
      composite.release()
      MemoryRawHttpContent(contentType, bytes)
    } else {
      MemoryRawHttpContent(contentType, Array.empty)
    }
  }
}

class TempFileActor(file: Path, ttl: FiniteDuration) extends Actor with ActorLogging {

  val channel = Files.newByteChannel(file, StandardOpenOption.WRITE, StandardOpenOption.APPEND)

  val selfDestruction = context.system.scheduler.scheduleOnce(ttl, self, PoisonPill)(context.dispatcher)

  override def receive = {
    case AppendBuf(buf, last) =>
      buf.nioBuffers foreach channel.write
      buf.release()
      if (last) channel.close()
  }

  override def postStop(): Unit = {
    selfDestruction.cancel()
    try {
      channel.close()
      Files.deleteIfExists(file)
    } catch {
      case e: Throwable => log.error(e, "Failed to destroy temp file: {}.", self.path.name)
    }
    super.postStop()
  }
}

case class AppendBuf(buf: ByteBuf, last: Boolean)
