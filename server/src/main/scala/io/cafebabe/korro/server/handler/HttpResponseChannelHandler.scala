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

import io.cafebabe.korro.api.http.HttpResponse
import io.cafebabe.korro.server.convert.HttpResponseConverter

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder

import java.util

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
@Sharable
class HttpResponseChannelHandler extends MessageToMessageEncoder[HttpResponse] {

  override def encode(ctx: ChannelHandlerContext, msg: HttpResponse, out: util.List[AnyRef]): Unit = {
    out add HttpResponseConverter.toNetty(msg)
  }
}
