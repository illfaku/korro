organization := "io.cafebabe.korro"
name := "korro"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.11",
  "com.typesafe" % "config" % "1.3.3",
  "org.json4s" %% "json4s-native" % "3.5.2",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "io.netty" % "netty-common" % "4.1.16.Final",
  "io.netty" % "netty-buffer" % "4.1.16.Final",
  "io.netty" % "netty-transport" % "4.1.16.Final",
  "io.netty" % "netty-handler" % "4.1.16.Final",
  "io.netty" % "netty-codec" % "4.1.16.Final",
  "io.netty" % "netty-codec-http" % "4.1.16.Final",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

enablePlugins(SbtOsgi)
osgiSettings
OsgiKeys.exportPackage := Seq("io.cafebabe.korro.*")
OsgiKeys.privatePackage := Seq.empty


resolvers += Resolver.sbtPluginRepo("releases")

javacOptions ++= Seq(
  "-Xlint:unchecked", "-Xlint:deprecation", "-source", "1.8", "-target", "1.8"
)

scalacOptions ++= Seq(
  "-encoding", "UTF-8", "-deprecation", "-unchecked", "-feature",
  "-language:implicitConversions", "-language:postfixOps", "-target:jvm-1.8"
)
