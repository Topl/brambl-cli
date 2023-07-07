import sbt._

object Dependencies {

  lazy val toplOrg = "co.topl"

  lazy val bramblVersion = "2.0.0-alpha1"
  val bramblSdk = toplOrg %% "brambl-sdk" % bramblVersion
  val bramblCrypto = toplOrg %% "crypto" % bramblVersion
  lazy val pbVersion = "2.0.0-alpha2"
  val protobufSpecs = toplOrg %% "protobuf-fs2" % pbVersion

  val grpcNetty =
    "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion

  val grpcRuntime =
    "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion

  lazy val catEffects = "org.typelevel" %% "cats-effect" % "3.3.12"
  lazy val scopt = "com.github.scopt" %% "scopt" % "4.0.1"
  lazy val munit = "org.scalameta" %% "munit" % "0.7.29" % "it,test"
  lazy val fs2Core = "co.fs2" %% "fs2-core" % "3.5.0"
  lazy val fs2IO = "co.fs2" %% "fs2-io" % "3.5.0"
  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
  lazy val sqlite = "org.xerial" % "sqlite-jdbc" % "3.41.2.1"
  lazy val fastparse = "com.lihaoyi" %% "fastparse" % "3.0.1"
  lazy val munitCatsEffects =  "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % "test"
}