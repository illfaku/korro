package io.cafebabe.korro.netty.handler

import io.cafebabe.korro.api.http.HttpParams.HttpParams
import io.cafebabe.korro.api.http.{HttpContent, HttpMessage, HttpRequest, HttpResponse, _}
import io.cafebabe.korro.netty.ByteBufUtils.toByteBuf

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, DefaultFileRegion}
import io.netty.handler.codec.MessageToMessageEncoder
import io.netty.handler.codec.http.{HttpMessage => NettyMessage, HttpMethod, _}

import java.util

/**
  * TODO: Add description.
  *
  * @author Vladimir Konstantinov
  */
@Sharable
class HttpMessageEncoder extends MessageToMessageEncoder[HttpMessage] {

  override def encode(ctx: ChannelHandlerContext, msg: HttpMessage, out: util.List[AnyRef]): Unit = {

    msg match {

      case req: HttpRequest =>
        val request = new DefaultHttpRequest(
          HttpVersion.HTTP_1_1, HttpMethod.valueOf(req.method), prepareUri(req.path, req.parameters)
        )
        setHeaders(request, req)
        out add request

      case res: HttpResponse =>
        val response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(res.status))
        setHeaders(response, res)
        out add response
    }

    if (msg.content.length > 0) {
      encodeContent(msg.content, out)
    } else {
      out add LastHttpContent.EMPTY_LAST_CONTENT
    }
  }

  private def encodeContent(content: HttpContent, out: util.List[AnyRef]): Unit = content match {

    case c: MemoryHttpContent =>
      out add new DefaultLastHttpContent(c.bytes)

    case c: FileHttpContent =>
      out add new DefaultFileRegion(c.file.toFile, c.pos, c.length - c.pos)
      out add LastHttpContent.EMPTY_LAST_CONTENT
  }

  private def prepareUri(path: String, parameters: HttpParams): String = {
    val encoder = new QueryStringEncoder(path)
    parameters foreach { case (name, values) => values.foreach(encoder.addParam(name, _)) }
    encoder.toString
  }

  private def setHeaders(nettyMsg: NettyMessage, msg: HttpMessage): Unit = {
    msg.headers foreach { case (name, values) => values.foreach(HttpHeaders.addHeader(nettyMsg, name, _)) }
    if (msg.content.length > 0) {
      HttpHeaders.setContentLength(nettyMsg, msg.content.length)
      HttpHeaders.addHeader(nettyMsg, HttpHeaders.Names.CONTENT_TYPE, msg.content.contentType.toString)
    }
  }
}
