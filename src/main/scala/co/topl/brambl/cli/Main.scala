package co.topl.brambl.cli

import cats.data.Validated
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.kernel.Resource
import co.topl.brambl.cli.impl.SimpleTransactionAlgebra
import co.topl.brambl.cli.impl.TransactionBuilderApi
import co.topl.brambl.cli.impl.WalletAlgebra
import co.topl.brambl.cli.impl.WalletStateAlgebra
import co.topl.brambl.cli.validation.BramblCliParamsValidatorModule
import co.topl.brambl.constants.NetworkConstants
import co.topl.brambl.wallet.WalletApi
import scopt.OParser

import java.sql.DriverManager

object Main extends IOApp {

  import BramblCliParamsValidatorModule._

  import BramblCliParamsParserModule._

  val dataApi = new DefaultDataApi[IO]()

  val walletApi = WalletApi.make(dataApi)

  def walletResource(name: String) = Resource
    .make(
      IO.delay(
        DriverManager.getConnection(
          s"jdbc:sqlite:${name}"
        )
      )
    )(conn => IO.delay(conn.close()))

  private def createWalletFromParams(
      params: BramblCliValidatedParams
  ): IO[Unit] = {
    val transactionBuilderApi = TransactionBuilderApi.make[IO](
      params.network.networkId,
      NetworkConstants.MAIN_LEDGER_ID
    )
    WalletAlgebra
      .make[IO](
        Main.this.walletApi,
        WalletStateAlgebra.make[IO](
          () => walletResource(params.walletFile),
          transactionBuilderApi
        )
      )
      .createWalletFromParams(params)
  }

  private def createSimpleTransactionFromParams(
      params: BramblCliValidatedParams
  ): IO[Unit] = {
    val transactionBuilderApi = TransactionBuilderApi.make[IO](
      params.network.networkId,
      NetworkConstants.MAIN_LEDGER_ID
    )
    val walletStateApi = WalletStateAlgebra.make[IO](
      () => walletResource(params.walletFile),
      transactionBuilderApi
    )
    val simplTransactionOps = SimpleTransactionAlgebra
      .make[IO](
        Main.this.dataApi,
        Main.this.walletApi,
        walletStateApi,
        transactionBuilderApi
      )
    walletStateApi.validateCurrentIndicesForFunds(
      params.fromParty,
      params.fromContract,
      params.someFromState
    ) flatMap {
      case Validated.Invalid(errors) =>
        IO.println("Invalid params") *> IO.println(
          errors.toList.mkString(", ")
        ) *> IO.print(OParser.usage(paramParser))
      case Validated.Valid(_) =>
        simplTransactionOps.createSimpleTransactionFromParams(
          params
        )
    }
  }

  def queryUtxoFromParams(params: BramblCliValidatedParams): IO[Unit] =
    ??? // TODO

  override def run(args: List[String]): IO[ExitCode] = {
    OParser.parse(paramParser, args, BramblCliParams()) match {
      case Some(params) =>
        val op = validateParams(params) match {
          case Validated.Valid(validateParams) =>
            (validateParams.mode, validateParams.subcmd) match {
              case (BramblCliMode.wallet, BramblCliSubCmd.init) =>
                createWalletFromParams(validateParams)
              case (BramblCliMode.simpletransaction, BramblCliSubCmd.create) =>
                createSimpleTransactionFromParams(validateParams)
              case (BramblCliMode.utxo, BramblCliSubCmd.query) =>
                queryUtxoFromParams(validateParams)
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
