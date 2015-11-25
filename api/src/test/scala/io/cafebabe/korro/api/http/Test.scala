package io.cafebabe.korro.api.http

import io.cafebabe.korro.api.http.HttpContent.{Json, Text}
import io.cafebabe.korro.api.http.HttpStatus.Ok

/**
  * TODO: Add description.
  *
  * @author Vladimir Konstantinov
  */
object Test extends App {

  val msg = HttpResponse(200, HttpParams.empty, HttpContent.memory(
    """""".getBytes(ContentType.DefaultCharset),
    ContentType(ContentType.Names.TextPlain, ContentType.DefaultCharset)
  ))

  val msg2 =

  msg match {
    case Ok(Json(json)) => println("Extracted json: " + json)
    case Ok(Text(text)) => println("Extracted text: " + text)
    case _ => println(msg)
  }
}
