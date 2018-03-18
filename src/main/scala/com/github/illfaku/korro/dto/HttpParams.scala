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
package com.github.illfaku.korro.dto

import scala.util.Try
import scala.util.control.NonFatal

class HttpParams(val entries: List[(String, String)]) {

  def +(entry: (String, Any)): HttpParams = new HttpParams((entry._1 -> entry._2.toString) :: entries)

  def ++(that: HttpParams): HttpParams = new HttpParams(entries ++ that.entries)

  def -(name: String): HttpParams = new HttpParams(entries.filterNot(_._1 equalsIgnoreCase name))

  def -(entry: (String, Any), ignoreCase: Boolean = false): HttpParams = this - (entry._1, entry._2, ignoreCase)

  def -(name: String, value: Any, ignoreCase: Boolean = false): HttpParams = {
    new HttpParams(entries filterNot { e =>
      e._1.equalsIgnoreCase(name) && (if (ignoreCase) e._2.equalsIgnoreCase(value.toString) else e._2 == value.toString)
    })
  }


  def contains(name: String): Boolean = entries.exists(_._1 equalsIgnoreCase name)

  def contains(entry: (String, Any), ignoreCase: Boolean = false): Boolean = contains(entry._1, entry._2, ignoreCase)

  def contains(name: String, value: Any, ignoreCase: Boolean = false): Boolean = entries exists { e =>
    e._1.equalsIgnoreCase(name) && (if (ignoreCase) e._2.equalsIgnoreCase(value.toString) else e._2 == value.toString)
  }

  def isEmpty: Boolean = entries.isEmpty

  def apply(name: String): String = getOrElse(name, throw new NoSuchElementException(name))

  def get(name: String): Option[String] = entries.find(_._1 equalsIgnoreCase name).map(_._2)

  def getOrElse(name: String, default: => String): String = get(name).getOrElse(default)

  def getAll(name: String): List[String] = entries.filter(_._1 equalsIgnoreCase name).map(_._2)


  def extract(name: String*): HttpParams.Extraction = new HttpParams.Extraction(name, name.flatMap(getAll))


  override lazy val toString: String = entries.map(e => e._1 + " = " + e._2).mkString("HttpParams(", ", ", ")")
}

object HttpParams {

  val empty: HttpParams = new HttpParams(Nil)

  def apply(entries: (String, Any)*): HttpParams = {
    new HttpParams(entries.map(e => e._1 -> e._2.toString).toList)
  }


  class MandatoryExtractor[V](name: String, as: String => V) {
    def unapply(params: HttpParams): Option[V] = params.get(name).flatMap(v => Try(as(v)).toOption)
  }

  class OptionalExtractor[V](name: String, as: String => V) {
    def unapply(params: HttpParams): Option[Option[V]] = Some(params.get(name).flatMap(v => Try(as(v)).toOption))
  }

  class ListExtractor[V](name: String, as: String => V) {
    def unapply(params: HttpParams): Option[List[V]] = Some(params.getAll(name).flatMap(v => Try(as(v)).toOption))
  }


  sealed trait ExtractionFailure
  case class Absent(name: String) extends ExtractionFailure
  case class Malformed(name: String, value: String, cause: Throwable) extends ExtractionFailure

  class Extraction(names: Seq[String], values: Seq[String]) {

    def mandatory[V](as: String => V): Either[ExtractionFailure, V] = {
      optional(as).right.flatMap(_.map(Right(_)).getOrElse(Left(Absent(names.head))))
    }

    def mandatory: Either[ExtractionFailure, String] = mandatory(identity)

    def optional[V](as: String => V): Either[ExtractionFailure, Option[V]] = {
      values.headOption map { value =>
        try {
          Right(Option(as(value)))
        } catch {
          case NonFatal(cause) => Left(Malformed(names.head, value, cause))
        }
      } getOrElse Right(None)
    }

    def optional: Either[ExtractionFailure, Option[String]] = optional(identity)

    def list[V](as: String => V): Either[ExtractionFailure, List[V]] = {
      val it = values.iterator
      def loop(result: List[V]): Either[ExtractionFailure, List[V]] = {
        if (it.hasNext) {
          val value = it.next
          try {
            loop(as(value) :: result)
          } catch {
            case NonFatal(cause) => Left(Malformed(names.head, value, cause))
          }
        } else {
          Right(result)
        }
      }
      loop(Nil)
    }

    def list: Either[ExtractionFailure, List[String]] = list(identity)
  }
}
