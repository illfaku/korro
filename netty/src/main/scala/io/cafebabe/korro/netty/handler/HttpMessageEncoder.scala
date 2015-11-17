package io.cafebabe.korro.netty.handler

import io.cafebabe.korro.api.http.HttpParams.HttpParams
import io.cafebabe.korro.api.http.HttpRequest
import io.cafebabe.korro.api.http.HttpResponse
import io.cafebabe.korro.api.http._
import io.cafebabe.korro.netty.ByteBufUtils
import io.cafebabe.korro.netty.convert.{HttpHeadersConverter, QueryParamsConverter}

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.{HttpMessage => NettyHttpMessage, _}

import java.util

/**
  * TODO: Add description.
  *
  * @author Vladimir Konstantinov
  */
class HttpMessageEncoder extends MessageToMessageEncoder[HttpMessage] {

  override def encode(ctx: ChannelHandlerContext, msg: HttpMessage, out: util.List[AnyRef]): Unit = {

    msg match {

      case req: HttpRequest =>
        val request = new DefaultHttpRequest(
          HttpVersion.HTTP_1_1, HttpMethod.valueOf(req.method), QueryParamsConverter.toNetty(req.path, req.parameters)
        )
        setHeaders(request, req)
        out add request

      case res: HttpResponse =>
        val response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(res.status))
        setHeaders(response, res)
        out add response
    }

    msg.content match {

      case content: MemoryHttpContent =>
        val buf = if (content.length > 0) ByteBufUtils.toByteBuf(content.bytes) else Unpooled.EMPTY_BUFFER
        val lastChunk = new DefaultLastHttpContent(buf)

      case content: FileHttpContent =>
    }
  }

  private def setHeaders(nettyMsg: NettyHttpMessage, msg: HttpMessage): Unit = {
    msg.headers foreach { case (name, values) => values.foreach(HttpHeaders.addHeader(nettyMsg, name, _)) }
    if (msg.content.length > 0) {
      HttpHeaders.setContentLength(nettyMsg, msg.content.length)
      HttpHeaders.addHeader(nettyMsg, HttpHeaders.Names.CONTENT_TYPE, msg.content.contentType.toString)
    }
  }
}
