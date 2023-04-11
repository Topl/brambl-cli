
import sbt._

object Dependencies {

  lazy val toplOrg = "com.github.Topl"


  lazy val bramblVersion = "5eca7c6"
  val bramblSc = "com.github.Topl"  % "BramblSc" % bramblVersion
  lazy val catEffects = "org.typelevel" %% "cats-effect" % "3.3.12"
  lazy val scopt = "com.github.scopt" %% "scopt" % "4.0.1"
  lazy val munit = "org.scalameta" %% "munit" % "0.7.29" % Test
  lazy val fs2Core = "co.fs2" %% "fs2-core" % "3.5.0"
  lazy val fs2IO = "co.fs2" %% "fs2-io" % "3.5.0"
  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
}
