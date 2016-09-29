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
package org.oxydev.korro.http.api

import java.text.DateFormat
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.{ISO_LOCAL_DATE_TIME, ISO_OFFSET_DATE_TIME, ISO_ZONED_DATE_TIME}
import java.time.{LocalDateTime, OffsetDateTime, ZonedDateTime}

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.control.{NoStackTrace, NonFatal}
import scala.util.{Failure, Success, Try}

class HttpParams(val entries: List[(String, String)]) {

  def +(entry: (String, Any)): HttpParams = new HttpParams((entry._1 -> entry._2.toString) :: entries)

  def ++(that: HttpParams): HttpParams = new HttpParams(entries ++ that.entries)

  def -(name: String): HttpParams = new HttpParams(entries.filterNot(_._1 equalsIgnoreCase name))

  def -(entry: (String, Any)): HttpParams = {
    new HttpParams(entries.filterNot(e => e._1.equalsIgnoreCase(entry._1) && e._2 == entry._2.toString))
  }


  def contains(name: String): Boolean = entries.exists(_._1 equalsIgnoreCase name)

  def contains(entry: (String, Any)): Boolean = contains(entry._1, entry._2)

  def contains(name: String, value: Any): Boolean = entries.exists(e => e._1.equalsIgnoreCase(name) && e._2 == value)

  def isEmpty: Boolean = entries.isEmpty

  def apply(name: String): String = getOrElse(name, throw new NoSuchElementException(name))

  def get(name: String): Option[String] = entries.find(_._1 equalsIgnoreCase name).map(_._2)

  def getOrElse(name: String, default: => String): String = get(name).getOrElse(default)

  def all(name: String): List[String] = entries.filter(_._1 equalsIgnoreCase name).map(_._2)


  import HttpParams.Extractions._

  def mandatory[V](name: String, f: Extraction[V]): Try[V] = entry(name).map(f).getOrElse(Failure(Absent(name)))

  def optional[V](name: String, f: Extraction[V]): Try[Option[V]] = {
    entry(name).map(f.andThen(_.map(Option(_)))).getOrElse(Success(None))
  }

  private def entry(name: String): Option[(String, String)] = entries.find(_._1 equalsIgnoreCase name)


  override lazy val toString: String = entries.mkString("HttpParams(", ", ", ")")
}

object HttpParams {

  val empty: HttpParams = new HttpParams(Nil)

  def apply(entries: (String, Any)*): HttpParams = new HttpParams(entries.map(e => e._1 -> e._2.toString).toList)

  object Extractions {

    sealed abstract class ExtractionFailure extends Throwable with NoStackTrace
    case class Absent(name: String) extends ExtractionFailure {
      override lazy val toString = s"Missing parameter: $name."
    }
    case class Malformed(name: String, value: String, cause: Throwable) extends ExtractionFailure {
      override lazy val toString = s"Invalid parameter: $name=$value. Cause: ${cause.getMessage}"
    }

    trait Extraction[V] extends (((String, String)) => Try[V]) { self =>
      def map[U](f: V => U): Extraction[U] = new Extraction[U] {
        override def apply(v: (String, String)): Try[U] = {
          val (name, value) = v
          self(v).map(f) recoverWith {
            case NonFatal(cause) => Failure(Malformed(name, value, cause))
          }
        }
      }
    }

    val asString: Extraction[String] = new Extraction[String] {
      override def apply(v: (String, String)): Either[ExtractionFailure, String] = Right(v._2)
    }

    val asLong = asString.map(_.toLong)
    val asInt = asString.map(_.toInt)
    val asShort = asString.map(_.toShort)
    val asByte = asString.map(_.toByte)
    val asBigInt = asString.map(BigInt(_))

    val asDouble = asString.map(_.toDouble)
    val asFloat = asString.map(_.toFloat)
    val asBigDecimal = asString.map(BigDecimal(_))

    val asBoolean = asString.map(_.toBoolean)

    def asDate(format: DateFormat) = asString.map(format.parse)
    def asTemporalAccessor(format: DateTimeFormatter) = asString.map(format.parse)

    val asIsoLocalDateTime = asTemporalAccessor(ISO_LOCAL_DATE_TIME).map(LocalDateTime.from)
    val asIsoOffsetDateTime = asTemporalAccessor(ISO_OFFSET_DATE_TIME).map(OffsetDateTime.from)
    val asIsoZonedDateTime = asTemporalAccessor(ISO_ZONED_DATE_TIME).map(ZonedDateTime.from)

    val asIsoDuration = asString.map(java.time.Duration.parse)

    val asDuration = asString.map(Duration.apply)
    val asFiniteDuration = asDuration.map(d => FiniteDuration(d.length, d.unit))
  }
}
