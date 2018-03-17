/*
 * Copyright 2016 Vladimir Konstantinov, Yuriy Gintsyak
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
package com.github.illfaku.korro.api

import java.text.DateFormat
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.time.{LocalDateTime, OffsetDateTime, ZonedDateTime}
import java.util.Date

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.control.NonFatal

class HttpParams private (val entries: List[(String, String)]) {

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


  def extract(name: String*): HttpParams.Extraction[String] = {
    new HttpParams.Extraction(name, name.flatMap(getAll), identity)
  }


  override lazy val toString: String = entries.map(e => e._1 + " = " + e._2).mkString("HttpParams(", ", ", ")")
}

object HttpParams {

  val empty: HttpParams = new HttpParams(Nil)

  def apply(entries: (String, Any)*): HttpParams = {
    new HttpParams(entries.map(e => e._1 -> e._2.toString).toList)
  }


  sealed trait ExtractionFailure
  case class Absent(name: String) extends ExtractionFailure
  case class Malformed(name: String, value: String, cause: Throwable) extends ExtractionFailure

  type Extractor[V] = String => V

  class Extraction[V](names: Seq[String], values: Seq[String], extractor: Extractor[V]) {

    def mandatory: Either[ExtractionFailure, V] = {
      optional.right.flatMap(_.map(Right(_)).getOrElse(Left(Absent(names.head))))
    }

    def optional: Either[ExtractionFailure, Option[V]] = {
      values.headOption map { value =>
        try {
          Right(Option(extractor(value)))
        } catch {
          case NonFatal(cause) => Left(Malformed(names.head, value, cause))
        }
      } getOrElse Right(None)
    }

    def list: Either[ExtractionFailure, List[V]] = {
      val it = values.iterator
      def loop(result: List[V]): Either[ExtractionFailure, List[V]] = {
        if (it.hasNext) {
          val value = it.next
          try {
            loop(extractor(value) :: result)
          } catch {
            case NonFatal(cause) => Left(Malformed(names.head, value, cause))
          }
        } else {
          Right(result)
        }
      }
      loop(Nil)
    }

    def map[U](f: V => U): Extraction[U] = new Extraction(names, values, extractor andThen f)
  }

  object Extractors {

    val asLong: Extractor[Long] = _.toLong
    val asInt: Extractor[Int] = _.toInt
    val asShort: Extractor[Short] = _.toShort
    val asByte: Extractor[Byte] = _.toByte
    val asBigInt: Extractor[BigInt] = BigInt(_)

    val asDouble: Extractor[Double] = _.toDouble
    val asFloat: Extractor[Float] = _.toFloat
    val asBigDecimal: Extractor[BigDecimal] = BigDecimal(_)

    val asBoolean: Extractor[Boolean] = _.toBoolean

    def asDate(format: DateFormat): Extractor[Date] = format.parse
    def asTemporalAccessor(format: DateTimeFormatter): Extractor[TemporalAccessor] = format.parse

    val asIsoZonedDateTime: Extractor[ZonedDateTime] = ZonedDateTime.parse
    val asIsoOffsetDateTime: Extractor[OffsetDateTime] = OffsetDateTime.parse
    val asIsoLocalDateTime: Extractor[LocalDateTime] = LocalDateTime.parse

    val asIsoDuration: Extractor[java.time.Duration] = java.time.Duration.parse

    val asDuration: Extractor[Duration] = Duration(_)
    val asFiniteDuration: Extractor[FiniteDuration] = asDuration.andThen(d => FiniteDuration(d.length, d.unit))
  }
}
