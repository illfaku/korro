import sbt._

object Dependency {

  object V {
    val Scala = "2.11.7"
    val Akka = "2.3.13"
    val Netty = "4.0.31.Final"
  }

  val cafebabeUtil = "io.cafebabe" %% "util" % "0.0.1-SNAPSHOT" % "provided"

  val akka = "com.typesafe.akka" %% "akka-actor" % V.Akka % "provided"

  val typesafeConfig = "com.typesafe" % "config" % "1.2.1" % "provided"
  val json4s = "org.json4s" %% "json4s-native" % "3.2.11" % "provided"
  val slf4j = "org.slf4j" % "slf4j-api" % "1.7.12" % "provided"

  val osgiCore = "org.osgi" % "org.osgi.core" % "5.0.0" % "provided"
  val bnd = "biz.aQute.bnd" % "biz.aQute.bnd.annotation" % "2.4.0" % "provided"

  val netty = Seq(
    "io.netty" % "netty-common" % V.Netty % "provided",
    "io.netty" % "netty-buffer" % V.Netty % "provided",
    "io.netty" % "netty-transport" % V.Netty % "provided",
    "io.netty" % "netty-handler" % V.Netty % "provided",
    "io.netty" % "netty-codec" % V.Netty % "provided",
    "io.netty" % "netty-codec-http" % V.Netty % "provided"
  )

  val scalatest = "org.scalatest" %% "scalatest" % "2.2.4" % "test"
}
