package io.cafebabe.korro.api.http

import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

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
  case class Absent(name: String) extends Failure
  case class Malformed(name: String, cause: Throwable) extends Failure


  object Extractions {

    trait HttpParamsExtraction[V] extends (((String, String)) => Either[Failure, V]) { self =>
      def map[U](f: V => U): HttpParamsExtraction[U] = new HttpParamsExtraction[U] {
        override def apply(v: (String, String)): Either[Failure, U] = {
          try {
            self.apply(v).right.map(f)
          } catch {
            case cause: Throwable => Left(Malformed(v._1, cause))
          }
        }
      }
    }

    class GenericExtraction[V](f: (String) => V) extends HttpParamsExtraction[V] {
      override def apply(v: (String, String)): Either[Failure, V] = {
        val (name, value) = v
        Try(f(value)) match {
          case Success(result) => Right(result)
          case Failure(cause) => Left(Malformed(name, cause))
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

    def asTemporalAccessor(format: DateTimeFormatter): HttpParamsExtraction[TemporalAccessor] = new GenericExtraction(format.parse)
  }
}
