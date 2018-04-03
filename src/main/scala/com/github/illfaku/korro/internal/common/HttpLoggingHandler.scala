/*
 * Copyright 2018 Vladimir Konstantinov
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
package com.github.illfaku.korro.internal.common

import com.github.illfaku.korro.config.HttpInstruction.BytesLoggingFormat
import com.github.illfaku.korro.util.logging.Logger

import io.netty.buffer.ByteBuf
import io.netty.channel._
import io.netty.handler.codec.http._
import io.netty.handler.codec.http.websocketx._

import java.nio.charset.StandardCharsets
import java.util.Base64

import scala.collection.JavaConverters._

private[internal] class HttpLoggingHandler(instructions: HttpInstructions) extends ChannelDuplexHandler {

  private val base64Encoder = Base64.getEncoder

  private val logger = Logger(instructions.httpLogger)

  private var logTemplate: String = _

  override def handlerAdded(ctx: ChannelHandlerContext) = {
    if (ctx.channel.isActive) logTemplate = prepareLogTemplate(ctx)
    super.handlerAdded(ctx)
  }

  override def channelActive(ctx: ChannelHandlerContext) = {
    if (logTemplate == null) logTemplate = prepareLogTemplate(ctx)
    super.channelActive(ctx)
  }

  private def prepareLogTemplate(ctx: ChannelHandlerContext): String = {
    "[0x" + ctx.channel.id.asShortText + ", L:" + String.valueOf(ctx.channel.localAddress) + " {} " +
      "R:" + String.valueOf(ctx.channel.remoteAddress) + "] {}"
  }

  private def debug(direction: Char, message: String): Unit = logger.debug(logTemplate, direction, message)

  private def trace(direction: Char, message: String): Unit = logger.trace(logTemplate, direction, message)

  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = {
    if (logger.isDebugEnabled) log('<', msg)
    super.channelRead(ctx, msg)
  }

  override def write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise): Unit = {
    if (logger.isDebugEnabled) log('>', msg)
    super.write(ctx, msg, promise)
  }

  private def log(direction: Char, msg: Any): Unit = msg match {

    case x: FullHttpRequest =>
      logRequest(direction, x)
      logContent(direction, x, end = true)

    case x: FullHttpResponse =>
      logResponse(direction, x)
      logContent(direction, x, end = true)

    case x: HttpRequest =>
      logRequest(direction, x)

    case x: HttpResponse =>
      logResponse(direction, x)

    case x: FileRegion =>
      debug(direction, "CONTENT FILE (" + x.count.toString + "B)")

    case x: LastHttpContent =>
      logContent(direction, x, end = true)

    case x: HttpContent =>
      logContent(direction, x, end = false)

    case x: TextWebSocketFrame if logger.isTraceEnabled =>
      trace(direction, "WS TEXT (" + x.content.readableBytes + "B): " + x.text)

    case x: BinaryWebSocketFrame if logger.isTraceEnabled =>
      logBinaryWs(direction, "WS BINARY", x.content)

    case x: ContinuationWebSocketFrame if logger.isTraceEnabled =>
      logBinaryWs(direction, "WS CONTINUATION", x.content)

    case x: PingWebSocketFrame if logger.isTraceEnabled =>
      logBinaryWs(direction, "WS PING", x.content)

    case x: PongWebSocketFrame if logger.isTraceEnabled =>
      logBinaryWs(direction, "WS PONG", x.content)

    case x: CloseWebSocketFrame if logger.isTraceEnabled =>
      trace(direction, "WS CLOSE (" + x.content.readableBytes + "B): " + x.statusCode + " " + x.reasonText)

    case _ => ()
  }

  private def logRequest(direction: Char, x: HttpRequest): Unit = {
    val builder = new StringBuilder
    builder ++= "REQUEST: " ++=
      String.valueOf(x.method) += ' ' ++=
      String.valueOf(x.uri) += ' ' ++=
      String.valueOf(x.protocolVersion)
    appendHeaders(builder, x.headers)
    debug(direction, builder.toString)
  }

  private def logResponse(direction: Char, x: HttpResponse): Unit = {
    val builder = new StringBuilder
    builder ++= "RESPONSE: " ++= String.valueOf(x.protocolVersion) += ' ' ++= String.valueOf(x.status)
    appendHeaders(builder, x.headers)
    debug(direction, builder.toString)
  }

  private def appendHeaders(builder: StringBuilder, headers: HttpHeaders): Unit = {
    if (logger.isTraceEnabled && !headers.isEmpty) {
      headers.asScala foreach { header =>
        builder ++= System.lineSeparator ++= header.getKey ++= ": " ++= header.getValue
      }
    }
  }

  private def logContent(direction: Char, x: HttpContent, end: Boolean): Unit = {
    val size = x.content.readableBytes
    val builder = new StringBuilder
    builder ++= "CONTENT"
    if (end) builder ++= " END"
    builder ++= " (" ++= size.toString ++= "B)"
    if (logger.isTraceEnabled && size > 0 && instructions.httpContentLogging != BytesLoggingFormat.Off) {
      builder ++= System.lineSeparator
      if (instructions.httpContentLogging == BytesLoggingFormat.Text) {
        builder ++= x.content.toString(StandardCharsets.UTF_8)
      } else {
        builder ++= base64Encoder.encodeToString(x.content)
      }
    }
    debug(direction, builder.toString)
  }

  private def logBinaryWs(direction: Char, kind: String, data: ByteBuf): Unit = {
    trace(direction, kind + " (" + data.readableBytes + "B): " + base64Encoder.encodeToString(data))
  }
}
