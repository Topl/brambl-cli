import Dependencies._
import org.scalajs.linker.interface.ModuleSplitStyle
import scala.sys.process.Process

lazy val scalacVersion = "2.13.8"



lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .in(file("./shared"))
  .enablePlugins(BuildInfoPlugin)
  .settings(commonSettings)
  .settings(
    organization := "co.topl",
    // sbt-BuildInfo plugin can write any (simple) data available in sbt at
    // compile time to a `case class BuildInfo` that it makes available at runtime.
    buildInfoKeys := Seq[BuildInfoKey](
      scalaVersion,
      sbtVersion,
      BuildInfoKey("laminarVersion" -> Dependencies.laminarVersion)
    ),
    // The BuildInfo case class is located in target/scala<version>/src_managed,
    // and with this setting, you'll need to `import com.raquo.buildinfo.BuildInfo`
    // to use it.
    buildInfoPackage := "co.topl.buildinfo"
    // Because we add BuildInfo to the `shared` project, this will be available
    // on both the client and the server, but you can also make it e.g. server-only.
  )
  .settings(
    libraryDependencies ++= List(
  "io.circe" %%% "circe-core" % Dependencies.circeVersion,
  "io.circe" %%% "circe-generic" % Dependencies.circeVersion,
  "io.circe" %%% "circe-parser" % Dependencies.circeVersion
    )
  )
  .jvmSettings(
    libraryDependencies ++= List(
      // This dependency lets us put @JSExportAll and similar Scala.js
      // annotations on data structures shared between JS and JVM.
      // With this library, on the JVM, these annotations compile to
      // no-op, which is exactly what we need.
      "org.scala-js" %% "scalajs-stubs" % "1.1.0"
    )
  )

lazy val gui = project
  .in(file("./gui"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= List(
      "com.raquo" %%% "laminar" % Dependencies.laminarVersion,
      "com.raquo" %%% "waypoint" % "7.0.0"

    ),
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
      // .withModuleSplitStyle(
      //   ModuleSplitStyle.SmallModulesFor(List("com.raquo.app")))
    },
    // Generated scala.js output will call your main() method to start your app.
    scalaJSUseMainModuleInitializer := true
  )
  .settings(
    // Ignore changes to .less and .css files when watching files with sbt.
    // With the suggested build configuration and usage patterns, these files are
    // not included in the scala.js output, so there is no need for sbt to watch
    // their contents. If sbt was also watching those files, editing them would
    // cause the entire Scala.js app to do a full reload, whereas right now we
    // have Vite watching those files, and it is able to hot-reload them without
    // reloading the entire application – much faster and smoother.
    watchSources := watchSources.value.filterNot { source =>
      source.base.getName.endsWith(".less") || source.base.getName
        .endsWith(".css")
    }
  )
  .settings(noPublish)
  .dependsOn(shared.js)


lazy val root = project
  .in(file("."))
  .settings(
    organization := "co.topl",
    name := "brambl-cli-umbrella"
    )
  .settings(noPublish)
  .aggregate(gui, cli, shared.jvm)


lazy val cli = project
  .in(file("./cli"))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(commonSettings)
  .settings(
    organization := "co.topl",
    name := "brambl-cli",
    fork := true,
    resolvers ++= Seq(
      Resolver.defaultLocal,
      "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/",
      "Sonatype Staging" at "https://s01.oss.sonatype.org/content/repositories/staging",
      "Sonatype Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots/",
      "Sonatype Releases" at "https://s01.oss.sonatype.org/content/repositories/releases/",
      "Bintray" at "https://jcenter.bintray.com/",
      "jitpack" at "https://jitpack.io"
    ),
    homepage := Some(url("https://github.com/Topl/brambl-cli")),
    licenses := List("MPL2.0" -> url("https://www.mozilla.org/en-US/MPL/2.0/")),
    ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org",
    sonatypeRepository := "https://s01.oss.sonatype.org/service/local",
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
    ),
    libraryDependencies ++= List(
      bramblSdk,
      bramblCrypto,
      bramblServiceKit,
      scopt,
      munit,
      fs2Core,
      fs2IO,
      logback,
      grpcNetty,
      grpcRuntime,
      sqlite,
      munitCatsEffects,
      fastparse,
      circeYaml,
      circeGenericJVM,
      monocleCore,
      monocleMacro,
      http4sEmber,
      http4sCirce,
      http4sDsl,
      log4cats
    )
  )
  .settings(
    assembly / mainClass := Some("co.topl.brambl.cli.Main"),
    assembly / assemblyJarName := "bramblcli.jar",

    // Gets rid of "(server / assembly) deduplicate: different file contents found in the following" errors
    // https://stackoverflow.com/questions/54834125/sbt-assembly-deduplicate-module-info-class
    assembly / assemblyMergeStrategy := {
      case x if x.contains("io.netty.versions.properties") => MergeStrategy.discard
      case path if path.endsWith("module-info.class") => MergeStrategy.discard
      case path =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(path)
    }
  )
  .dependsOn(shared.jvm)


lazy val noPublish = Seq(
  publishLocal / skip := true,
  publish / skip := true
)


lazy val commonSettings = Seq(
  scalaVersion := scalacVersion,
  scalacOptions ++= Seq(
    "-deprecation",
    "-Ymacro-annotations",
    "-Ywarn-unused"
  ),
  semanticdbEnabled := true,
  semanticdbVersion := scalafixSemanticdb.revision
)
