import com.typesafe.sbt.osgi.SbtOsgi
import sbt.Keys._

lazy val compileJdkSettings = Seq(
  javacOptions ++= Seq(
    "-Xlint:unchecked", "-Xlint:deprecation", "-source", "1.8", "-target", "1.8"
  ),
  scalacOptions ++= Seq(
    "-encoding", "UTF-8", "-deprecation", "-unchecked", "-optimize", "-feature",
    "-language:implicitConversions", "-language:postfixOps", "-target:jvm-1.8"
  )
)

lazy val osgiSettings = SbtOsgi.osgiSettings ++ Seq(
  OsgiKeys.exportPackage := Seq("io.cafebabe.http.server.api.*"),
  OsgiKeys.privatePackage := Seq("io.cafebabe.http.server.impl.*"),
  OsgiKeys.importPackage := Seq("!aQute.bnd.annotation.*", "*"),
  OsgiKeys.additionalHeaders := Map(
    "Bundle-Name" -> "Cafebabe HTTP",
    "Service-Component" -> "*"
  )
)

lazy val root = (project in file(".")).
  settings(compileJdkSettings: _*).
  settings(osgiSettings: _*).
  settings(
    organization := "io.cafebabe",
    name := "cafebabe-http",
    version := "0.0.1-SNAPSHOT",

    scalaVersion := "2.11.6",
    libraryDependencies ++= Seq(
      "io.cafebabe" %% "util" % "0.0.1-SNAPSHOT" % "provided",

      "com.typesafe" % "config" % "1.2.1" % "provided",
      "com.typesafe.akka" %% "akka-actor" % "2.3.11" % "provided",
      "org.json4s" %% "json4s-native" % "3.2.11" % "provided",
      "org.slf4j" % "slf4j-api" % "1.7.10" % "provided",

      "org.osgi" % "org.osgi.core" % "5.0.0" % "provided",
      "biz.aQute.bnd" % "biz.aQute.bnd.annotation" % "2.4.0" % "provided",

      "io.netty" % "netty-common" % "4.0.29.Final" % "provided",
      "io.netty" % "netty-buffer" % "4.0.29.Final" % "provided",
      "io.netty" % "netty-transport" % "4.0.29.Final" % "provided",
      "io.netty" % "netty-handler" % "4.0.29.Final" % "provided",
      "io.netty" % "netty-codec" % "4.0.29.Final" % "provided",
      "io.netty" % "netty-codec-http" % "4.0.29.Final" % "provided",

      "org.scalatest" %% "scalatest" % "2.2.4" % "test"
    )
  )
