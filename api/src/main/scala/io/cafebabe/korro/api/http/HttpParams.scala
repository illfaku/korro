package io.cafebabe.korro.api.http

import scala.util.{Failure, Success, Try}

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
object HttpParams {
  type HttpParams = Map[String, List[String]]
  type HttpParamsExtraction[V] = ((String, String)) => Either[Failure, V]

  def apply(headers: (String, Any)*): HttpParams = headers.groupBy(_._1).mapValues(_.map(_._2.toString).toList)

  implicit class Extractor(params: HttpParams) {
    def mandatory[V](name: String)(f: HttpParamsExtraction[V]): Either[Failure, V] = {
      one(name).map(Right(_)).getOrElse(Left(Absent(name))).right.flatMap(f)
    }
    def optional[V](name: String)(f: HttpParamsExtraction[V]): Either[Failure, Option[V]] = {
      one(name).map(f.andThen(_.right.map(Some(_)))).getOrElse(Right(None))
    }
    private def one(name: String): Option[(String, String)] = params.get(name).flatMap(_.headOption).map(name -> _)
  }

  trait Failure
  case class Absent(name: String) extends Failure
  case class Malformed(name: String, value: String) extends Failure

  class GenericExtraction[V](f: (String) => V) extends HttpParamsExtraction[V] {
    override def apply(v: (String, String)): Either[Failure, V] = {
      val (name, value) = v
      Try(f(value)) match {
        case Success(newVal) => Right(newVal)
        case Failure(t) => Left(Malformed(name, value))
      }
    }
  }

  object Extractions {
    val asString: HttpParamsExtraction[String] = new GenericExtraction(_.toString)
    val asLong: HttpParamsExtraction[Long] = new GenericExtraction(_.toLong)
    val asDouble: HttpParamsExtraction[Double] = new GenericExtraction(_.toDouble)
    val asBoolean: HttpParamsExtraction[Boolean] = new GenericExtraction(_.toBoolean)
  }
}
