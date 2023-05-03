import sbt._

object Dependencies {

  lazy val toplOrg = "co.topl"

  lazy val bramblVersion = "2.0.3+51-b6c91082-SNAPSHOT"
  val bramblSdk = toplOrg %% "brambl-sdk" % bramblVersion
  val bramblCrypto = toplOrg %% "crypto" % bramblVersion

  val grpcNetty =
    "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion

  val grpcRuntime =
    "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion

  lazy val catEffects = "org.typelevel" %% "cats-effect" % "3.3.12"
  lazy val scopt = "com.github.scopt" %% "scopt" % "4.0.1"
  lazy val munit = "org.scalameta" %% "munit" % "0.7.29" % Test
  lazy val fs2Core = "co.fs2" %% "fs2-core" % "3.5.0"
  lazy val fs2IO = "co.fs2" %% "fs2-io" % "3.5.0"
  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
  lazy val sqlite = "org.xerial" % "sqlite-jdbc" % "3.41.2.1"
}
