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
import co.topl.brambl.cli.controllers.PartiesController
import co.topl.brambl.cli.controllers.ContractsController
import co.topl.brambl.cli.impl.PartyStorageAlgebra
import co.topl.brambl.cli.impl.ContractStorageAlgebra

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

  private def contractModeSubcmds(
      validateParams: BramblCliValidatedParams
  ): IO[String] = {
    val contractStorageAlgebra = ContractStorageAlgebra.make[IO](
      walletResource(validateParams.walletFile)
    )
    validateParams.subcmd match {
      case BramblCliSubCmd.list =>
        new ContractsController(
          contractStorageAlgebra
        )
          .listContracts()
      case BramblCliSubCmd.add =>
        new ContractsController(
          contractStorageAlgebra
        )
          .addContract(
            validateParams.contractName,
            validateParams.lockTemplate
          )
    }
  }
  private def partiesModeSubcmds(
      validateParams: BramblCliValidatedParams
  ) = {
    val partyStorageAlgebra = PartyStorageAlgebra.make[IO](
      walletResource(validateParams.walletFile)
    )
    validateParams.subcmd match {
      case BramblCliSubCmd.add =>
        new PartiesController(partyStorageAlgebra)
          .addParty(validateParams.partyName)
      case BramblCliSubCmd.list =>
        new PartiesController(partyStorageAlgebra)
          .listParties()
    }
  }

  private def walletModeSubcmds(
      validateParams: BramblCliValidatedParams
  ) = {
    val walletController = new WalletController(
      walletResource(validateParams.walletFile)
    )
    validateParams.subcmd match {
      case BramblCliSubCmd.exportvk =>
        walletController.exportVk(validateParams)
      case BramblCliSubCmd.importvks =>
        walletController.importVk(validateParams)
      case BramblCliSubCmd.init =>
        walletController.createWalletFromParams(validateParams)
      case BramblCliSubCmd.currentaddress =>
        walletController.currentaddress(validateParams)
    }
  }

  private def simpleTransactionSubcmds(
      validateParams: BramblCliValidatedParams
  ): IO[String] = validateParams.subcmd match {
    case BramblCliSubCmd.broadcast =>
      new SimpleTransactionController(
        walletResource(validateParams.walletFile),
        channelResource(
          validateParams.host,
          validateParams.bifrostPort
        )
      ).broadcastSimpleTransactionFromParams(validateParams)
    case BramblCliSubCmd.prove =>
      new SimpleTransactionController(
        walletResource(validateParams.walletFile),
        channelResource(
          validateParams.host,
          validateParams.bifrostPort
        )
      ).proveSimpleTransactionFromParams(validateParams)
    case BramblCliSubCmd.create =>
      new SimpleTransactionController(
        walletResource(validateParams.walletFile),
        channelResource(
          validateParams.host,
          validateParams.bifrostPort
        )
      ).createSimpleTransactionFromParams(validateParams)
  }

  private def genusQuerySubcmd(
      validateParams: BramblCliValidatedParams
  ): IO[String] = validateParams.subcmd match {
    case BramblCliSubCmd.utxobyaddress =>
      new GenusQueryController(
        walletResource(validateParams.walletFile),
        channelResource(
          validateParams.host,
          validateParams.bifrostPort
        )
      ).queryUtxoFromParams(validateParams)
  }

  private def bifrostQuerySubcmd(
      validateParams: BramblCliValidatedParams
  ): IO[String] = validateParams.subcmd match {
    case BramblCliSubCmd.blockbyheight =>
      new BifrostQueryController(
        channelResource(
          validateParams.host,
          validateParams.bifrostPort
        )
      ).blockByHeight(validateParams)
    case BramblCliSubCmd.blockbyid =>
      new BifrostQueryController(
        channelResource(
          validateParams.host,
          validateParams.bifrostPort
        )
      ).blockById(validateParams)
    case BramblCliSubCmd.transactionbyid =>
      new BifrostQueryController(
        channelResource(
          validateParams.host,
          validateParams.bifrostPort
        )
      ).fetchTransaction(validateParams)
  }

  override def run(args: List[String]): IO[ExitCode] = {
    OParser.parse(paramParser, args, BramblCliParams()) match {
      case Some(params) =>
        val op: IO[String] = validateParams(params) match {
          case Validated.Valid(validateParams) =>
            validateParams.mode match {
              case BramblCliMode.contracts =>
                contractModeSubcmds(validateParams)
              case BramblCliMode.parties =>
                partiesModeSubcmds(validateParams)
              case BramblCliMode.wallet =>
                walletModeSubcmds(validateParams)
              case BramblCliMode.simpletransaction =>
                simpleTransactionSubcmds(validateParams)
              case BramblCliMode.genusquery =>
                genusQuerySubcmd(validateParams)
              case BramblCliMode.bifrostquery =>
                bifrostQuerySubcmd(validateParams)
            }
          case Validated.Invalid(errors) =>
            IO(
              "Invalid params\n" +
                OParser.usage(paramParser) + "\n" +
                errors.toList.mkString(", ")
            )
        }
        for {
          output <- op
          _ <- IO(println(output))
        } yield ExitCode.Success
      case _ =>
        IO.pure(ExitCode.Error)
    }
  }

}
