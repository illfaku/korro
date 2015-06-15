import sbt.Keys._

lazy val root = (project in file(".")).
  settings(
    organization := "io.cafebabe",
    name := "http",
    version := "0.0.1-SNAPSHOT",

    scalaVersion := "2.11.6",
    libraryDependencies ++= Seq(
      "io.cafebabe" %% "util" % "0.0.1-SNAPSHOT" % "provided",

      "com.typesafe" % "config" % "1.3.0" % "provided",
      "com.typesafe.akka" %% "akka-actor" % "2.3.9" % "provided",
      "com.typesafe.akka" %% "akka-slf4j" % "2.3.9" % "provided",
      "org.json4s" %% "json4s-native" % "3.2.11" % "provided",
      "org.slf4j" % "slf4j-api" % "1.7.10" % "provided",

      "io.netty" % "netty-common" % "4.0.28.Final" % "provided",
      "io.netty" % "netty-buffer" % "4.0.28.Final" % "provided",
      "io.netty" % "netty-transport" % "4.0.28.Final" % "provided",
      "io.netty" % "netty-handler" % "4.0.28.Final" % "provided",
      "io.netty" % "netty-codec" % "4.0.28.Final" % "provided",
      "io.netty" % "netty-codec-http" % "4.0.28.Final" % "provided",

      "org.scalatest" %% "scalatest" % "2.2.4" % "test"
    )
  )
