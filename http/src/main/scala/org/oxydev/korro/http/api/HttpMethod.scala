/*
 * Copyright (C) 2015, 2016  Vladimir Konstantinov, Yuriy Gintsyak
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
package org.oxydev.korro.http.api

import org.oxydev.korro.util.protocol.http.QueryStringCodec

/**
 * HTTP request methods as constructors and handy extractors of request.
 * <br><br>
 * Extractors usage:
 * {{{
 *   val req: HttpRequest = ???
 *   req match {
 *     case HttpMethod.Get(r) => ???
 *     case HttpMethod.Post(r) => ???
 *   }
 * }}}
 *
 * @author Vladimir Konstantinov
 */
object HttpMethod {

  val Get = new HttpMethod("GET")
  val Post = new HttpMethod("POST")
  val Put = new HttpMethod("PUT")
  val Delete = new HttpMethod("DELETE")
  val Head = new HttpMethod("HEAD")
  val Connect = new HttpMethod("CONNECT")
  val Options = new HttpMethod("OPTIONS")
  val Trace = new HttpMethod("TRACE")

  private val methodMap = Map(
    Get.name -> Get,
    Post.name -> Post,
    Put.name -> Put,
    Delete.name -> Delete,
    Head.name -> Head,
    Connect.name -> Connect,
    Options.name -> Options,
    Trace.name -> Trace
  )

  def apply(name: String): HttpMethod = {
    require(name != null, "name is null")
    require(name.trim.nonEmpty, "name is empty")
    methodMap.getOrElse(name, new HttpMethod(name))
  }
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class HttpMethod(val name: String) {

  def apply(
    path: String,
    parameters: HttpParams = HttpParams.empty,
    content: HttpContent = HttpContent.empty,
    headers: HttpParams = HttpParams.empty
  ): HttpRequest = {
    val query = if (parameters.isEmpty) "" else "?" + QueryStringCodec.encode(parameters.entries)
    new HttpRequest(this, s"$path$query", headers, content)
  }

  def unapply(req: HttpRequest): Option[HttpRequest] = if (this == req.method) Some(req) else None


  def canEqual(other: Any): Boolean = other.isInstanceOf[HttpMethod]

  override def equals(other: Any): Boolean = other match {
    case that: HttpMethod => (that canEqual this) && name == that.name
    case _ => false
  }

  override lazy val hashCode = name.hashCode

  override val toString = name
}
