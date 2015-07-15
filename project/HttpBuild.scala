import Dependency._
import com.typesafe.sbt.osgi.{OsgiKeys, SbtOsgi}
import sbt.Keys._
import sbt._

object HttpBuild extends Build {

  lazy val basicSettings = Seq(
    organization := "io.cafebabe",
    name := "cafebabe-http",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := V.Scala
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

  lazy val osgiSettings = SbtOsgi.osgiSettings ++ Seq(
    OsgiKeys.exportPackage := Seq("io.cafebabe.http.server.api.*"),
    OsgiKeys.privatePackage := Seq("io.cafebabe.http.server.impl.*"),
    OsgiKeys.importPackage := Seq("!aQute.bnd.annotation.*", "*"),
    OsgiKeys.additionalHeaders := Map(
      "Bundle-Name" -> "Cafebabe HTTP",
      "Service-Component" -> "*"
    )
  )

  lazy val dependencies = Seq(libraryDependencies ++= Seq(
    akka, typesafeConfig, json4s, slf4j, osgiCore, bnd, scalatest
  ) ++ netty)

  lazy val root = Project(
    id = "http",
    base = file("."),
    dependencies = Seq(util),
    settings = basicSettings ++ compileJdkSettings ++ osgiSettings ++ dependencies
  )

  lazy val util = RootProject(uri("git://github.com/yet-another-cafebabe/util.git"))
}
