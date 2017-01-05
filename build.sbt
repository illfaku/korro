// Settings ------------------------------------------------------------------------------------------------------------

lazy val commonSettings = Seq(

  organization := "org.oxydev",
  version := "0.3.0-SNAPSHOT",
  scalaVersion := "2.12.1",

  licenses := Seq("Apache 2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
  homepage := Some(url("https://github.com/oxy-development/korro")),
  organizationHomepage := Some(url("https://github.com/oxy-development")),

  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  pomExtra :=
    <scm>
      <url>git@github.com:oxy-development/korro.git</url>
      <connection>scm:git:git@github.com:oxy-development/korro.git</connection>
    </scm>
    <developers>
      <developer>
        <id>illfaku</id>
        <name>Vladimir Konstantinov</name>
        <url>https://github.com/illfaku</url>
      </developer>
    </developers>
)

lazy val compileJdkSettings = Seq(
  javacOptions ++= Seq(
    "-Xlint:unchecked", "-Xlint:deprecation", "-source", "1.8", "-target", "1.8"
  ),
  scalacOptions ++= Seq(
    "-encoding", "UTF-8", "-deprecation", "-unchecked", "-feature",
    "-language:implicitConversions", "-language:postfixOps", "-target:jvm-1.8"
  )
)


// Dependencies --------------------------------------------------------------------------------------------------------

val reflect = "org.scala-lang" % "scala-reflect" % "2.12.1"

val akka = "com.typesafe.akka" %% "akka-actor" % "2.4.16"

val typesafeConfig = "com.typesafe" % "config" % "1.3.1"
val json4s = "org.json4s" %% "json4s-native" % "3.5.0"
val slf4j = "org.slf4j" % "slf4j-api" % "1.7.22"

val nettyCommon = "io.netty" % "netty-common" % "4.1.6.Final"
val nettyBuffer = "io.netty" % "netty-buffer" % "4.1.6.Final"
val nettyTransport = "io.netty" % "netty-transport" % "4.1.6.Final"
val nettyHandler = "io.netty" % "netty-handler" % "4.1.6.Final"
val nettyCodec = "io.netty" % "netty-codec" % "4.1.6.Final"
val nettyHttp = "io.netty" % "netty-codec-http" % "4.1.6.Final"

val scalatest = "org.scalatest" %% "scalatest" % "3.0.1" % Test


// Projects ------------------------------------------------------------------------------------------------------------

lazy val root = Project(
  id = "korro",
  base = file("."),
  settings = commonSettings ++ Seq(publishArtifact := false),
  aggregate = Seq(http, util)
)

lazy val http = Project(
  id = "korro-http",
  base = file("http"),
  dependencies = Seq(util),
  settings = commonSettings ++ compileJdkSettings ++ Seq(
    OsgiKeys.exportPackage := Seq(
      "org.oxydev.korro.http",
      "org.oxydev.korro.http.api.*",
      "org.oxydev.korro.http.tools.*"
    ),
    OsgiKeys.privatePackage := Seq("org.oxydev.korro.http.internal.*"),
    OsgiKeys.additionalHeaders := Map("Bundle-Name" -> "Korro HTTP")
  ) ++ Seq(libraryDependencies ++= Seq(
    akka, typesafeConfig, json4s, slf4j, scalatest,
    nettyCommon, nettyBuffer, nettyTransport, nettyHandler, nettyCodec, nettyHttp
  ))
).enablePlugins(SbtOsgi)

lazy val util = Project(
  id = "korro-util",
  base = file("util"),
  settings = commonSettings ++ compileJdkSettings ++ Seq(
    OsgiKeys.exportPackage := Seq("org.oxydev.korro.util.*"),
    OsgiKeys.additionalHeaders := Map("Bundle-Name" -> "Korro Utilities")
  ) ++ Seq(libraryDependencies ++= Seq(
    akka, typesafeConfig, json4s, slf4j, scalatest
  ))
).enablePlugins(SbtOsgi)
