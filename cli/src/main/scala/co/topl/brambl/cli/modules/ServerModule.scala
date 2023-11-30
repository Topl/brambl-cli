package co.topl.brambl.cli.modules

import cats.Id
import cats.data.Kleisli
import cats.effect.IO
import cats.effect._
import co.topl.brambl.cli.BramblCliParams
import co.topl.brambl.cli.BramblCliSubCmd
import co.topl.brambl.cli.impl.FullTxOps
import co.topl.brambl.cli.impl.WalletModeHelper
import co.topl.brambl.codecs.AddressCodecs
import co.topl.brambl.dataApi.GenusQueryAlgebra
import co.topl.brambl.servicekit.FellowshipStorageApi
import co.topl.brambl.servicekit.TemplateStorageApi
import co.topl.shared.models.AssetTokenBalanceDTO
import co.topl.shared.models.BalanceRequestDTO
import co.topl.shared.models.BalanceResponseDTO
import co.topl.shared.models.FellowshipDTO
import co.topl.shared.models.GroupTokenBalanceDTO
import co.topl.shared.models.LvlBalance
import co.topl.shared.models.SeriesTokenBalanceDTO
import co.topl.shared.models.TemplateDTO
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
import co.topl.shared.models.SimpleErrorDTO

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

  def walletService(validateParams: BramblCliParams) = HttpRoutes.of[IO] {
    case req @ POST -> Root / "balance" =>
      implicit val balanceReqdecoder: EntityDecoder[IO, BalanceRequestDTO] =
        jsonOf[IO, BalanceRequestDTO]
      (for {
        input <- req.as[BalanceRequestDTO]
        balanceEither <- WalletModeHelper[IO](
          walletStateAlgebra(
            validateParams.walletFile
          ),
          GenusQueryAlgebra
            .make[IO](
              channelResource(
                validateParams.host,
                validateParams.bifrostPort,
                validateParams.secureConnection
              )
            )
        ).getBalance(
          None,
          Some(input.fellowship),
          Some(input.template),
          input.interaction.map(_.toInt)
        ).map(
          _.left
            .map { e =>
              new IllegalArgumentException(e)
            }
        )
        balances <- IO.fromEither(balanceEither)
        res <- Ok(
          balances
            .foldLeft(
              BalanceResponseDTO(
                "0",
                List.empty,
                List.empty,
                List.empty
              )
            ){(acc, x) =>
              (x: @unchecked) match { // we have filtered out Unknown tokens
                case LvlBalance(b) => acc.copy(lvlBalance = b)
                case GroupTokenBalanceDTO(g, b) =>
                  acc.copy(groupBalances =
                    acc.groupBalances :+ GroupTokenBalanceDTO(g, b)
                  )
                case SeriesTokenBalanceDTO(id, balance) =>
                  acc.copy(seriesBalances =
                    acc.seriesBalances :+ SeriesTokenBalanceDTO(id, balance)
                  )
                case AssetTokenBalanceDTO(group, series, balance) =>
                  acc.copy(assetBalances =
                    acc.assetBalances :+ AssetTokenBalanceDTO(
                      group,
                      series,
                      balance
                    )
                  )
              }
            }
            .asJson
        )
      } yield res).handleErrorWith { t =>
        t.printStackTrace()
        InternalServerError(
          SimpleErrorDTO(t.getMessage()).asJson,
          headers.`Content-Type`(MediaType.text.html)
        )
      }
    case GET -> Root / "fellowships" =>
      val fellowshipStorageAlgebra = FellowshipStorageApi.make[IO](
        walletResource(validateParams.walletFile)
      )
      fellowshipStorageAlgebra.findFellowships().flatMap { fellowships =>
        Ok(fellowships.map(x => FellowshipDTO(x.xIdx, x.name)).asJson)
      }
    case GET -> Root / "templates" =>
      val templateStorageAlgebra = TemplateStorageApi.make[IO](
        walletResource(validateParams.walletFile)
      )
      import co.topl.brambl.cli.views.WalletModelDisplayOps._
      import io.circe.parser.parse
      import co.topl.brambl.codecs.LockTemplateCodecs._
      import cats.implicits._
      for {
        templates <- templateStorageAlgebra.findTemplates()
        resTemplates <- templates.traverse { x =>
          IO(
            (for {
              json <- parse(x.lockTemplate)
              decoded <- decodeLockTemplate[Id](json)
            } yield TemplateDTO(
              x.yIdx,
              x.name,
              serialize[Id](decoded)
            )).toOption.get
          )
        }
        res <- Ok(resTemplates.asJson)
      } yield res

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
            "/api/wallet" -> walletService(validateParams),
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
