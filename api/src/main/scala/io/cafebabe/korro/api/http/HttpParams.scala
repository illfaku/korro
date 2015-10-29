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
import java.time.{ZonedDateTime, OffsetDateTime, LocalDateTime}
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.util.Date

import scala.util.{Failure, Success, Try}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object HttpParams {

  type HttpParams = Map[String, List[String]]


  val empty: HttpParams = Map.empty

  def apply(headers: (String, Any)*): HttpParams = headers.groupBy(_._1).mapValues(_.map(_._2.toString).toList)


  implicit class Extractor(params: HttpParams) {

    def mandatory[V](name: String)(f: Extractions.HttpParamsExtraction[V]): Either[Failure, V] = {
      one(name).map(Right(_)).getOrElse(Left(Absent(name))).right.flatMap(f)
    }

    def optional[V](name: String)(f: Extractions.HttpParamsExtraction[V]): Either[Failure, Option[V]] = {
      one(name).map(f.andThen(_.right.map(Some(_)))).getOrElse(Right(None))
    }

    private def one(name: String): Option[(String, String)] = params.get(name).flatMap(_.headOption).map(name -> _)

    private implicit val ord = Ordering.String.on[(String, List[String])](_._1)
    def asString: String = {
      params.toList.sorted.foldLeft(new StringBuilder) { (b, e) =>
        b.append(e._1).append("=")
        e._2.sorted.addString(b, ",").append(";")
      } toString()
    }
  }


  sealed trait Failure
  case class Absent(name: String) extends Failure {
    override lazy val toString = s"Missing parameter: $name."
  }
  case class Malformed(name: String, value: String, cause: Throwable) extends Failure {
    override lazy val toString = s"Invalid parameter: $name=$value. Cause: ${cause.getMessage}"
  }


  object Extractions {

    trait HttpParamsExtraction[V] extends (((String, String)) => Either[Failure, V]) { self =>
      def map[U](f: V => U): HttpParamsExtraction[U] = new HttpParamsExtraction[U] {
        override def apply(v: (String, String)): Either[Failure, U] = {
          val (name, value) = v
          try {
            self.apply(v).right.map(f)
          } catch {
            case cause: Throwable => Left(Malformed(name, value, cause))
          }
        }
      }
    }

    class GenericExtraction[V](f: (String) => V) extends HttpParamsExtraction[V] {
      override def apply(v: (String, String)): Either[Failure, V] = {
        val (name, value) = v
        Try(f(value)) match {
          case Success(result) => Right(result)
          case Failure(cause) => Left(Malformed(name, value, cause))
        }
      }
    }

    val asString: HttpParamsExtraction[String] = new GenericExtraction(_.toString)

    val asLong: HttpParamsExtraction[Long] = new GenericExtraction(_.toLong)
    val asInt: HttpParamsExtraction[Int] = asLong.map(_.toInt)
    val asShort: HttpParamsExtraction[Short] = asLong.map(_.toShort)
    val asByte: HttpParamsExtraction[Byte] = asLong.map(_.toByte)
    val asBigInt: HttpParamsExtraction[BigInt] = new GenericExtraction(BigInt(_))

    val asDouble: HttpParamsExtraction[Double] = new GenericExtraction(_.toDouble)
    val asFloat: HttpParamsExtraction[Float] = asDouble.map(_.toFloat)
    val asBigDecimal: HttpParamsExtraction[BigDecimal] = new GenericExtraction(BigDecimal(_))

    val asBoolean: HttpParamsExtraction[Boolean] = new GenericExtraction(_.toBoolean)

    def asDate(format: DateFormat): HttpParamsExtraction[Date] = new GenericExtraction(format.parse)
    def asTemporalAccessor(format: DateTimeFormatter): HttpParamsExtraction[TemporalAccessor] = new GenericExtraction(format.parse)

    val asIsoLocalDateTime: HttpParamsExtraction[LocalDateTime] = asTemporalAccessor(DateTimeFormatter.ISO_LOCAL_DATE_TIME).map(LocalDateTime.from)
    val asIsoOffsetDateTime: HttpParamsExtraction[OffsetDateTime] = asTemporalAccessor(DateTimeFormatter.ISO_OFFSET_DATE_TIME).map(OffsetDateTime.from)
    val asIsoZonedDateTime: HttpParamsExtraction[ZonedDateTime] = asTemporalAccessor(DateTimeFormatter.ISO_ZONED_DATE_TIME).map(ZonedDateTime.from)
  }
}
