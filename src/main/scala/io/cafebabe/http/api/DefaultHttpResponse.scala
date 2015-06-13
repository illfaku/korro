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
package io.cafebabe.http.api

/**
 * @author Vladimir Konstantinov
 * @version 1.0 (6/13/2015)
 */
class DefaultHttpResponse(
  override val status: Int,
  override val content: String,
  override val headers: Map[String, String]
) extends HttpResponse {
  def this(status: Int) = this(status, "", Map.empty)
  def this(status: Int, content: String) = this(status, content, Map.empty)
  def this(status: Int, headers: Map[String, String]) = this(status, "", headers)
}
