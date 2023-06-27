package co.topl.brambl.cli

import cats.data.Validated
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.cli.controllers.GenusQueryController
import co.topl.brambl.cli.controllers.SimpleTransactionController
import co.topl.brambl.cli.controllers.WalletController
import co.topl.brambl.cli.controllers.BifrostQueryController
import co.topl.brambl.cli.validation.BramblCliParamsValidatorModule
import io.grpc.ManagedChannelBuilder
import scopt.OParser

import java.sql.DriverManager

object Main extends IOApp {

  import BramblCliParamsValidatorModule._

  import BramblCliParamsParserModule._

  def channelResource[F[_]: Sync](address: String, port: Int) = {
    Resource
      .make {
        Sync[F].delay(
          ManagedChannelBuilder
            .forAddress(address, port)
            .usePlaintext()
            .build
        )
      }(channel => Sync[F].delay(channel.shutdown()))
  }

  def walletResource(name: String) = Resource
    .make(
      IO.delay(
        DriverManager.getConnection(
          s"jdbc:sqlite:${name}"
        )
      )
    )(conn => IO.delay(conn.close()))

  override def run(args: List[String]): IO[ExitCode] = {
    OParser.parse(paramParser, args, BramblCliParams()) match {
      case Some(params) =>
        val op = validateParams(params) match {
          case Validated.Valid(validateParams) =>
            (validateParams.mode, validateParams.subcmd) match {
              case (BramblCliMode.wallet, BramblCliSubCmd.init) =>
                (new WalletController(
                  walletResource(validateParams.walletFile)
                ))
                  .createWalletFromParams(validateParams)
              case (BramblCliMode.wallet, BramblCliSubCmd.currentaddress) =>
                (new WalletController(
                  walletResource(validateParams.walletFile)
                ))
                  .currentaddress(validateParams)
              case (
                    BramblCliMode.simpletransaction,
                    BramblCliSubCmd.broadcast
                  ) =>
                new SimpleTransactionController(
                  walletResource(validateParams.walletFile),
                  channelResource(
                    validateParams.host,
                    validateParams.bifrostPort
                  )
                ).broadcastSimpleTransactionFromParams(validateParams)
              case (BramblCliMode.simpletransaction, BramblCliSubCmd.prove) =>
                new SimpleTransactionController(
                  walletResource(validateParams.walletFile),
                  channelResource(
                    validateParams.host,
                    validateParams.bifrostPort
                  )
                ).proveSimpleTransactionFromParams(validateParams)
              case (BramblCliMode.simpletransaction, BramblCliSubCmd.create) =>
                new SimpleTransactionController(
                  walletResource(validateParams.walletFile),
                  channelResource(
                    validateParams.host,
                    validateParams.bifrostPort
                  )
                ).createSimpleTransactionFromParams(validateParams)
              case (BramblCliMode.genusquery, BramblCliSubCmd.utxobyaddress) =>
                new GenusQueryController(
                  walletResource(validateParams.walletFile),
                  channelResource(
                    validateParams.host,
                    validateParams.bifrostPort
                  )
                ).queryUtxoFromParams(validateParams)
              case (
                    BramblCliMode.bifrostquery,
                    BramblCliSubCmd.blockbyheight
                  ) =>
                new BifrostQueryController(
                  channelResource(
                    validateParams.host,
                    validateParams.bifrostPort
                  )
                ).blockByHeight(validateParams)
              case (
                    BramblCliMode.bifrostquery,
                    BramblCliSubCmd.blockbyid
                  ) =>
                new BifrostQueryController(
                  channelResource(
                    validateParams.host,
                    validateParams.bifrostPort
                  )
                ).blockById(validateParams)
              case (
                    BramblCliMode.bifrostquery,
                    BramblCliSubCmd.transactionbyid
                  ) =>
                new BifrostQueryController(
                  channelResource(
                    validateParams.host,
                    validateParams.bifrostPort
                  )
                ).fetchTransaction(validateParams)
            }
          case Validated.Invalid(errors) =>
            IO.println("Invalid params") *> IO.println(
              errors.toList.mkString(", ")
            ) *> IO.print(OParser.usage(paramParser))
        }
        for {
          _ <- op
        } yield ExitCode.Success
      case _ =>
        IO.pure(ExitCode.Error)
    }
  }

}
