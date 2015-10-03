import com.typesafe.sbt.osgi.{OsgiKeys, SbtOsgi}
import sbt.Keys._
import sbt._

object HttpBuild extends Build {

  lazy val basicSettings = Seq(
    organization := "io.cafebabe.korro",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := Dependency.V.Scala
  )

  lazy val compileJdkSettings = Seq(
    javacOptions ++= Seq(
      "-Xlint:unchecked", "-Xlint:deprecation", "-source", "1.8", "-target", "1.8"
    ),
    scalacOptions ++= Seq(
      "-encoding", "UTF-8", "-deprecation", "-unchecked", "-optimize", "-feature",
      "-language:implicitConversions", "-language:postfixOps", "-target:jvm-1.8"
    )
  )

  lazy val root = Project(
    id = "korro",
    base = file("."),
    settings = basicSettings
  ) aggregate (api, server, client)

  lazy val api = Project(
    id = "korro-api",
    base = file("api"),
    dependencies = Seq(util),
    settings = basicSettings ++ compileJdkSettings ++ OsgiSettings.api ++ Dependencies.api
  )

  lazy val server = Project(
    id = "korro-server",
    base = file("server"),
    dependencies = Seq(api, util),
    settings = basicSettings ++ compileJdkSettings ++ OsgiSettings.server ++ Dependencies.server
  )

  lazy val client = Project(
    id = "korro-client",
    base = file("client"),
    dependencies = Seq(api, util),
    settings = basicSettings ++ compileJdkSettings ++ OsgiSettings.client ++ Dependencies.client
  )

  lazy val util = RootProject(uri("git://github.com/yet-another-cafebabe/util.git"))
}

object OsgiSettings {

  lazy val api = SbtOsgi.osgiSettings ++ Seq(
    OsgiKeys.exportPackage := Seq("io.cafebabe.korro.api.*"),
    OsgiKeys.additionalHeaders := Map("Bundle-Name" -> "Korro API")
  )

  lazy val server = SbtOsgi.osgiSettings ++ Seq(
    OsgiKeys.privatePackage := Seq("io.cafebabe.korro.server.*"),
    OsgiKeys.importPackage := Seq("!aQute.bnd.annotation.*", "*"),
    OsgiKeys.additionalHeaders := Map(
      "Bundle-Name" -> "Korro Server",
      "Service-Component" -> "*"
    )
  )

  lazy val client = SbtOsgi.osgiSettings ++ Seq(
    OsgiKeys.privatePackage := Seq("io.cafebabe.korro.client.*"),
    OsgiKeys.importPackage := Seq("!aQute.bnd.annotation.*", "*"),
    OsgiKeys.additionalHeaders := Map(
      "Bundle-Name" -> "Korro Client",
      "Service-Component" -> "*"
    )
  )
}

object Dependencies {

  import Dependency._

  lazy val api = deps(akka, json4s, scalatest)

  lazy val server = deps(
    akka, typesafeConfig, json4s, slf4j, osgiCore, bnd, scalatest,
    nettyCommon, nettyBuffer, nettyTransport, nettyHandler, nettyCodec, nettyHttp
  )

  lazy val client = deps(
    akka, typesafeConfig, json4s, slf4j, osgiCore, bnd, scalatest,
    nettyCommon, nettyBuffer, nettyTransport, nettyHandler, nettyCodec, nettyHttp
  )

  private def deps(modules: ModuleID*) = Seq(libraryDependencies ++= modules)
}

object Dependency {

  object V {
    val Scala = "2.11.7"
    val Akka = "2.4.0"
    val Netty = "4.0.32.Final"
  }

  val cafebabeUtil = "io.cafebabe" %% "util" % "0.0.1-SNAPSHOT" % "provided"

  val akka = "com.typesafe.akka" %% "akka-actor" % V.Akka % "provided"

  val typesafeConfig = "com.typesafe" % "config" % "1.3.0" % "provided"
  val json4s = "org.json4s" %% "json4s-native" % "3.2.11" % "provided"
  val slf4j = "org.slf4j" % "slf4j-api" % "1.7.12" % "provided"

  val osgiCore = "org.osgi" % "org.osgi.core" % "5.0.0" % "provided"
  val bnd = "biz.aQute.bnd" % "biz.aQute.bnd.annotation" % "2.4.0" % "provided"

  val nettyCommon = "io.netty" % "netty-common" % V.Netty % "provided"
  val nettyBuffer = "io.netty" % "netty-buffer" % V.Netty % "provided"
  val nettyTransport = "io.netty" % "netty-transport" % V.Netty % "provided"
  val nettyHandler = "io.netty" % "netty-handler" % V.Netty % "provided"
  val nettyCodec = "io.netty" % "netty-codec" % V.Netty % "provided"
  val nettyHttp = "io.netty" % "netty-codec-http" % V.Netty % "provided"

  val scalatest = "org.scalatest" %% "scalatest" % "2.2.5" % "test"
}