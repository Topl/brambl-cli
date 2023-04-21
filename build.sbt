import Dependencies._

scalaVersion := "2.13.8"

name := "brambl-cli"
organization := "co.topl"

  resolvers ++= Seq(
      Resolver.defaultLocal,
    "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/",
    "Sonatype Staging" at "https://s01.oss.sonatype.org/content/repositories/staging",
    "Sonatype Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots/",
    "Bintray" at "https://jcenter.bintray.com/",
    "jitpack" at "https://jitpack.io"
    )

libraryDependencies += bramblSc
libraryDependencies += scopt
libraryDependencies += munit
libraryDependencies += fs2Core
libraryDependencies += fs2IO
libraryDependencies += logback

scalacOptions += "-Ymacro-annotations"

scalacOptions += "-Ywarn-unused"

semanticdbEnabled := true

semanticdbVersion := scalafixSemanticdb.revision

homepage := Some(url("https://github.com/Topl/brambl-cli"))
licenses := List("MPL2.0" -> url("https://www.mozilla.org/en-US/MPL/2.0/"))
sonatypeCredentialHost := "s01.oss.sonatype.org"
sonatypeRepository := "https://s01.oss.sonatype.org/service/local"
developers := List(
Developer(
    "mundacho",
    "Edmundo Lopez Bobeda",
    "e.lopez@topl.me",
    url("https://github.com/mundacho")
),
Developer(
    "scasplte2",
    "James Aman",
    "j.aman@topl.me",
    url("https://github.com/scasplte2")
)
)