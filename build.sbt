organization := "com.github.illfaku"
name := "korro"

scalaVersion := "2.12.4"

javacOptions ++= Seq(
  "-Xlint:unchecked", "-Xlint:deprecation", "-source", "1.8", "-target", "1.8"
)

scalacOptions ++= Seq(
  "-encoding", "UTF-8", "-deprecation", "-unchecked", "-feature",
  "-language:implicitConversions", "-language:postfixOps", "-target:jvm-1.8"
)

resolvers += Resolver.sbtPluginRepo("releases")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.11",
  "com.typesafe" % "config" % "1.3.3",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "io.netty" % "netty-common" % "4.1.22.Final",
  "io.netty" % "netty-buffer" % "4.1.22.Final",
  "io.netty" % "netty-transport" % "4.1.22.Final",
  "io.netty" % "netty-handler" % "4.1.22.Final",
  "io.netty" % "netty-codec" % "4.1.22.Final",
  "io.netty" % "netty-codec-http" % "4.1.22.Final",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

enablePlugins(SbtOsgi)
osgiSettings
OsgiKeys.exportPackage := Seq("com.github.illfaku.korro.*")
OsgiKeys.privatePackage := Seq.empty


licenses := Seq("Apache 2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0"))
homepage := Some(url("https://github.com/illfaku/korro"))
organizationHomepage := Some(url("https://github.com/illfaku"))

publishMavenStyle := true
publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}
publishArtifact in Test := false
pomIncludeRepository := { _ => false }
pomExtra :=
  <scm>
    <url>git@github.com:illfaku/korro.git</url>
    <connection>scm:git:git@github.com:illfaku/korro.git</connection>
  </scm>
  <developers>
    <developer>
      <id>illfaku</id>
      <name>Vladimir Konstantinov</name>
      <url>https://github.com/illfaku</url>
    </developer>
  </developers>
