import Dependencies._

scalaVersion := "2.13.8"

name := "brambl-cli"
organization := "co.topl"
version := "1.0.0"

libraryDependencies += brambl
libraryDependencies += bramblCommon
libraryDependencies += slf4j
libraryDependencies += scopt
libraryDependencies += munit
libraryDependencies += fs2Core
libraryDependencies += fs2IO

scalacOptions += "-Ymacro-annotations"

scalacOptions += "-Ywarn-unused"

semanticdbEnabled := true

semanticdbVersion := scalafixSemanticdb.revision