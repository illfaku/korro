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
package io.cafebabe.korro.util.protocol.jsonrpc

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object JsonRpcException {

  def apply(code: Int, message: String, id: Int = 0) = new JsonRpcException(code, message, id)

  implicit def fromError(e: JsonRpcError): JsonRpcException = new JsonRpcException(e.code, e.message, e.id)
  implicit def toError(e: JsonRpcException): JsonRpcError = JsonRpcError(e.getCode, e.getMessage, e.getId)
}

class JsonRpcException(code: Int, message: String, id: Int) extends Exception {
  def getCode: Int = code
  override def getMessage: String = message
  def getId: Int = id
}
