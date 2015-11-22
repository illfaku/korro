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
package io.cafebabe.korro.api.http

import java.text.DateFormat
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.{ISO_LOCAL_DATE_TIME, ISO_OFFSET_DATE_TIME, ISO_ZONED_DATE_TIME}
import java.time.{LocalDateTime, OffsetDateTime, ZonedDateTime}
import java.util.NoSuchElementException

import scala.concurrent.duration.{Duration, FiniteDuration}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object HttpParams {

  val empty: HttpParams = new HttpParams(Nil)

  def apply(entries: (String, Any)*): HttpParams = new HttpParams(entries.map(e => e._1 -> e._2.toString))

  object Extractions {

    sealed trait ExtractionFailure
    case class Absent(name: String) extends ExtractionFailure {
      override lazy val toString = s"Missing parameter: $name."
    }
    case class Malformed(name: String, value: String, cause: Throwable) extends ExtractionFailure {
      override lazy val toString = s"Invalid parameter: $name=$value. Cause: ${cause.getMessage}"
    }

    trait Extraction[V] extends (((String, String)) => Either[ExtractionFailure, V]) { self =>
      def map[U](f: V => U): Extraction[U] = new Extraction[U] {
        override def apply(v: (String, String)): Either[ExtractionFailure, U] = {
          val (name, value) = v
          try {
            self.apply(v).right.map(f)
          } catch {
            case cause: Throwable => Left(Malformed(name, value, cause))
          }
        }
      }
    }

    val asString: Extraction[String] = new Extraction[String] {
      override def apply(v: (String, String)): Either[ExtractionFailure, String] = Right(v._2)
    }

    val asLong = asString.map(_.toLong)
    val asInt = asLong.map(_.toInt)
    val asShort = asLong.map(_.toShort)
    val asByte = asLong.map(_.toByte)
    val asBigInt = asString.map(BigInt(_))

    val asDouble = asString.map(_.toDouble)
    val asFloat = asDouble.map(_.toFloat)
    val asBigDecimal = asString.map(BigDecimal(_))

    val asBoolean = asString.map(_.toBoolean)

    def asDate(format: DateFormat) = asString.map(format.parse)
    def asTemporalAccessor(format: DateTimeFormatter) = asString.map(format.parse)

    val asIsoLocalDateTime = asTemporalAccessor(ISO_LOCAL_DATE_TIME).map(LocalDateTime.from)
    val asIsoOffsetDateTime = asTemporalAccessor(ISO_OFFSET_DATE_TIME).map(OffsetDateTime.from)
    val asIsoZonedDateTime = asTemporalAccessor(ISO_ZONED_DATE_TIME).map(ZonedDateTime.from)

    val asIsoDuration = asString.map(java.time.Duration.parse)

    val asDuration = asString.map(Duration.create)
    val asFiniteDuration = asDuration.map(d => FiniteDuration(d.length, d.unit))
  }
}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
class HttpParams(val entries: Iterable[(String, String)]) {

  def +(entry: (String, Any)): HttpParams = ++(HttpParams(entry))

  def ++(that: HttpParams): HttpParams = new HttpParams(entries ++ that.entries)


  def apply(name: String): String = get(name).getOrElse(throw new NoSuchElementException(name))

  def get(name: String): Option[String] = all(name).headOption

  def all(name: String): Iterable[String] = entries.filter(_._1 == name).map(_._2)


  import HttpParams.Extractions._

  def mandatory[V](name: String)(f: Extraction[V]): Either[ExtractionFailure, V] = {
    entry(name).map(Right(_)).getOrElse(Left(Absent(name))).right.flatMap(f)
  }

  def optional[V](name: String)(f: Extraction[V]): Either[ExtractionFailure, Option[V]] = {
    entry(name).map(f.andThen(_.right.map(Some(_)))).getOrElse(Right(None))
  }

  private def entry(name: String): Option[(String, String)] = entries.find(_._1 == name)
}
