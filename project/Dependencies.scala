import sbt._

object Dependencies {

  lazy val toplOrg = "co.topl"

  lazy val bramblVersion = "2.0.0-alpha5+1-ab025d5c-SNAPSHOT"
  val bramblSdk = toplOrg %% "brambl-sdk" % bramblVersion

  val monocleCore = "dev.optics" %% "monocle-core" % "3.2.0"

  val monocleMacro = "dev.optics" %% "monocle-macro" % "3.2.0"

  val bramblCrypto = toplOrg %% "crypto" % bramblVersion
  val bramblServiceKit = toplOrg %% "service-kit" % bramblVersion

  val bramblJitPack = "com.github.Topl" % "BramblSc" % "fc834fa"

  val grpcNetty =
    "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion

  val grpcRuntime =
    "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion

  lazy val catEffects = "org.typelevel" %% "cats-effect" % "3.3.12"
  lazy val scopt = "com.github.scopt" %% "scopt" % "4.1.0"
  lazy val munit = "org.scalameta" %% "munit" % "1.0.0-M8" % "it,test"
  lazy val fs2Core = "co.fs2" %% "fs2-core" % "3.5.0"
  lazy val fs2IO = "co.fs2" %% "fs2-io" % "3.5.0"
  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
  lazy val sqlite = "org.xerial" % "sqlite-jdbc" % "3.41.2.1"
  lazy val fastparse = "com.lihaoyi" %% "fastparse" % "3.0.1"
  lazy val munitCatsEffects =  "org.typelevel" %% "munit-cats-effect" % "2.0.0-M1" % "it,test"
  lazy val circeYaml = "io.circe" %% "circe-yaml-v12" % "0.14.3-RC3"
  lazy val circeGeneric = "io.circe" %% "circe-generic" % "0.15.0-M1"
}