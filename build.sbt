import sbt.Keys._

lazy val root = (project in file(".")).
  settings(
    organization := "io.cafebabe",
    name := "http",
    version := "0.0.1",

    scalaVersion := "2.11.6",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "2.2.4" % "test"
    )
  )
