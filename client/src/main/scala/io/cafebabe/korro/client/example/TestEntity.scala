package io.cafebabe.korro.client.example

import io.cafebabe.korro.client.HttpGet

/**
 * Created by ygintsyak on 19.06.15.
 */
class TestEntity(v1:String, v2:String) extends HttpGet {

  override def headers = Seq(
    "is_cafebabe_" -> "predefined value",
    "is_cafebabe_" -> s"this.v2=$v2"
  )

  override def params = Seq(
    "param1" -> v1,
    "param2" -> v2,
    "param3" -> "Const Value"
  )
}
