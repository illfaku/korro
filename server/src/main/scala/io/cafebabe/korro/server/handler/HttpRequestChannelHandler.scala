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
package io.cafebabe.korro.server.handler

import io.cafebabe.korro.api.http.HttpStatus.BadRequest
import io.cafebabe.korro.netty.convert.{ConversionFailure, HttpRequestConverter}
import io.cafebabe.korro.server.actor.HttpResponseSender
import io.cafebabe.korro.util.config.wrapped

import akka.actor.ActorContext
import com.typesafe.config.Config
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}

import scala.concurrent.duration._

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
@Sharable
class HttpRequestChannelHandler(config: Config)(implicit context: ActorContext)
  extends SimpleChannelInboundHandler[RoutedHttpRequest] {

  private val requestTimeout = config.findFiniteDuration("HTTP.requestTimeout").getOrElse(60 seconds)

  override def channelRead0(ctx: ChannelHandlerContext, msg: RoutedHttpRequest): Unit = {
    HttpRequestConverter.fromNetty(msg.req) match {
      case Right(request) =>
        implicit val sender = HttpResponseSender.create(ctx, requestTimeout)
        context.actorSelection(msg.route.actor) ! request
      case Left(fail: ConversionFailure) =>
        ctx.writeAndFlush(BadRequest(fail.toString)).addListener(ChannelFutureListener.CLOSE)
    }
  }
}
