import com.typesafe.sbt.osgi.{OsgiKeys, SbtOsgi}
import sbt.Keys._
import sbt._

object HttpBuild extends Build {

  lazy val basicSettings = Seq(

    organization := "org.oxydev",
    version := "0.3.0-SNAPSHOT",
    scalaVersion := Dependency.V.Scala,

    licenses := Seq("GNU Lesser General Public License" -> url("http://www.gnu.org/licenses/lgpl.html")),
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
      "-encoding", "UTF-8", "-deprecation", "-unchecked", "-optimize", "-feature",
      "-language:implicitConversions", "-language:postfixOps", "-target:jvm-1.8"
    )
  )

  lazy val root = Project(
    id = "korro",
    base = file("."),
    settings = basicSettings ++ Seq(publishArtifact := false)
  ) aggregate (http, util)

  lazy val http = Project(
    id = "korro-http",
    base = file("http"),
    dependencies = Seq(util),
    settings = basicSettings ++ compileJdkSettings ++ OsgiSettings.http ++ Dependencies.http
  )

  lazy val util = Project(
    id = "korro-util",
    base = file("util"),
    settings = basicSettings ++ compileJdkSettings ++ OsgiSettings.util ++ Dependencies.util
  )
}

object OsgiSettings {

  lazy val http = SbtOsgi.osgiSettings ++ Seq(
    OsgiKeys.privatePackage := Seq("org.oxydev.korro.http.internal.*"),
    OsgiKeys.exportPackage := Seq("org.oxydev.korro.http.api.*,org.oxydev.korro.http.actor.*"),
    OsgiKeys.additionalHeaders := Map("Bundle-Name" -> "Korro HTTP")
  )

  lazy val util = SbtOsgi.osgiSettings ++ Seq(
    OsgiKeys.exportPackage := Seq("org.oxydev.korro.util.*"),
    OsgiKeys.additionalHeaders := Map("Bundle-Name" -> "Korro Utilities")
  )
}

object Dependencies {

  import Dependency._

  lazy val http = deps(
    akka, typesafeConfig, json4s, slf4j, scalatest,
    nettyCommon, nettyBuffer, nettyTransport, nettyHandler, nettyCodec, nettyHttp
  )

  lazy val util = deps(akka, typesafeConfig, json4s, slf4j, scalatest)

  private def deps(modules: ModuleID*) = Seq(libraryDependencies ++= modules)
}

object Dependency {

  object V {
    val Scala = "2.11.7"
    val Akka = "2.4.2"
    val Netty = "4.0.34.Final"
  }

  val akka = "com.typesafe.akka" %% "akka-actor" % V.Akka

  val typesafeConfig = "com.typesafe" % "config" % "1.3.0"
  val json4s = "org.json4s" %% "json4s-native" % "3.3.0"
  val slf4j = "org.slf4j" % "slf4j-api" % "1.7.18"

  val nettyCommon = "io.netty" % "netty-common" % V.Netty
  val nettyBuffer = "io.netty" % "netty-buffer" % V.Netty
  val nettyTransport = "io.netty" % "netty-transport" % V.Netty
  val nettyHandler = "io.netty" % "netty-handler" % V.Netty
  val nettyCodec = "io.netty" % "netty-codec" % V.Netty
  val nettyHttp = "io.netty" % "netty-codec-http" % V.Netty

  val scalatest = "org.scalatest" %% "scalatest" % "2.2.6" % "test"
}
