package co.topl.brambl.cli.modules

import cats.data.Kleisli
import cats.effect.IO
import cats.effect._
import co.topl.brambl.cli.BramblCliParams
import co.topl.brambl.cli.BramblCliSubCmd
import co.topl.brambl.cli.http.WalletHttpService
import co.topl.brambl.cli.impl.FullTxOps
import co.topl.brambl.codecs.AddressCodecs
import co.topl.shared.models.TxRequest
import co.topl.shared.models.TxResponse
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.staticcontent.resourceServiceBuilder

import java.nio.file.Files
import scopt.OParser
import co.topl.brambl.cli.BramblCliParamsParserModule

trait ServerModule extends FellowshipsModeModule with WalletModeModule {

  lazy val httpService = HttpRoutes.of[IO] {

    // You must serve the index.html file that loads your frontend code for
    // every url that is defined in your frontend (Waypoint) routes, in order
    // for users to be able to navigate to these URLs from outside of your app.
    case request @ GET -> Root =>
      StaticFile
        .fromResource("/static/index.html", Some(request))
        .getOrElseF(InternalServerError())

    // This route covers all URLs under `/app`, including `/app` and `/app/`.
    case request @ GET -> "app" /: _ =>
      StaticFile
        .fromResource("/static/index.html", Some(request))
        .getOrElseF(InternalServerError())

    // Vite moves index.html into the public directory, but we don't want
    // users to navigate manually to /index.html in the browser, because
    // that route is not defined in Waypoint, we use `/` instead.
    case GET -> Root / "index.html" =>
      TemporaryRedirect(headers.Location(Uri.fromString("/").toOption.get))
  }

  def apiServices(validateParams: BramblCliParams) = HttpRoutes.of[IO] {
    case req @ POST -> Root / "send" =>
      implicit val txReqDecoder: EntityDecoder[IO, TxRequest] =
        jsonOf[IO, TxRequest]

      for {
        input <- req.as[TxRequest]
        result <- FullTxOps.sendFunds(
          validateParams.network,
          validateParams.password,
          validateParams.walletFile,
          validateParams.someKeyFile.get,
          input.fromFellowship,
          input.fromTemplate,
          input.fromInteraction.map(_.toInt),
          Some(input.fromFellowship),
          Some(input.fromTemplate),
          input.fromInteraction.map(_.toInt),
          AddressCodecs.decodeAddress(input.address).toOption,
          input.amount.toLong,
          input.fee.toLong,
          Files.createTempFile("txFile", ".pbuf").toAbsolutePath().toString(),
          Files
            .createTempFile("provedTxFile", ".pbuf")
            .toAbsolutePath()
            .toString(),
          validateParams.host,
          validateParams.bifrostPort,
          validateParams.secureConnection
        )
        resp <- Ok(TxResponse(result).asJson)
      } yield resp
  }

  def serverSubcmd(
      validateParams: BramblCliParams
  ): IO[Either[String, String]] = validateParams.subcmd match {
    case BramblCliSubCmd.invalid =>
      IO.pure(
        Left(
          OParser.usage(
            BramblCliParamsParserModule.serverMode
          ) + "\nA subcommand needs to be specified"
        )
      )
    case BramblCliSubCmd.init =>
      val staticAssetsService = resourceServiceBuilder[IO]("/static").toRoutes
      val logger =
        org.typelevel.log4cats.slf4j.Slf4jLogger.getLoggerFromName[IO]("App")
      (for {
        notFoundResponse <- Resource.make(
          NotFound(
            """<!DOCTYPE html>
          |<html>
          |<body>
          |<h1>Not found</h1>
          |<p>The page you are looking for is not found.</p>
          |<p>This message was generated on the server.</p>
          |</body>
          |</html>""".stripMargin('|'),
            headers.`Content-Type`(MediaType.text.html)
          )
        )(_ => IO.unit)

        app = {
          val router = Router.define(
            "/" -> httpService,
            "/api/wallet" -> WalletHttpService(
              walletStateAlgebra(
                validateParams.walletFile
              ),
              channelResource(
                validateParams.host,
                validateParams.bifrostPort,
                validateParams.secureConnection
              ),
              walletResource(validateParams.walletFile)
            ).walletService(
              validateParams.network.name,
              validateParams.network.networkId.toString()
            ),
            "/api/tx" -> apiServices(validateParams)
          )(default = staticAssetsService)

          Kleisli[IO, Request[IO], Response[IO]] { request =>
            router.run(request).getOrElse(notFoundResponse)
          }
        }

        _ <- EmberServerBuilder
          .default[IO]
          .withIdleTimeout(ServerConfig.idleTimeOut)
          .withHost(ServerConfig.host)
          .withPort(ServerConfig.port)
          .withHttpApp(app)
          .withLogger(logger)
          .build

      } yield {
        Right(
          s"Server started on ${ServerConfig.host}:${ServerConfig.port}"
        )
      }).allocated
        .map(_._1)
        .handleErrorWith { e =>
          IO {
            Left(e.getMessage)
          }
        } >> IO.never
  }
}
